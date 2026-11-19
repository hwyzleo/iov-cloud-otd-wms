package net.hwyz.iov.cloud.otd.wms.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.InboxRecords;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ArrivalInboundTaskCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CancelInboundTaskCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmInboundTaskCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleInboundCancellationCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleInboundRequestedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ItemReasonDto;
import net.hwyz.iov.cloud.otd.wms.service.application.port.InboundFactPublisher;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InboundTaskState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.common.util.OrderNumGenerator;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.InboundTask;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboundOrderFact;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InboundTaskCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboundTaskRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.service.StorageHierarchyValidator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 交付库待入库应用服务
 * <p>
 * 到货请求命令整批原子受理（PENDING + 库存 OUTBOUND → IN_TRANSIT）；到货、入库、取消为 MPT 业务动作；
 * 每次接受的动作在本地事务内写入任务、库存与 Outbox 事实。
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundTaskAppService {

    private static final String ACTION_ARRIVAL = "inboundTask.arrival";
    private static final String ACTION_INBOUND = "inboundTask.inbound";
    private static final String ACTION_CANCEL = "inboundTask.cancellation";

    private final InboundTaskRepository inboundTaskRepository;
    private final InboundOrderRepository inboundOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final InboxRepository inboxRepository;
    private final InboundFactPublisher inboundFactPublisher;
    private final StorageHierarchyValidator storageHierarchyValidator;
    private final MptActionGuard mptActionGuard;

    /**
     * 查询待入库任务
     *
     * @param criteria 查询条件
     * @return 任务列表
     */
    public List<InboundTask> search(InboundTaskCriteria criteria) {
        return inboundTaskRepository.search(criteria);
    }

    /**
     * 按主键查询任务
     *
     * @param id 主键
     * @return 任务
     */
    public InboundTask getById(Long id) {
        return inboundTaskRepository.findById(id).orElse(null);
    }

    /**
     * 消费到货请求命令
     *
     * @param cmd 到货请求
     * @return 消费结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResultDto consumeRequested(VehicleInboundRequestedCmd cmd) {
        Optional<InboxRecord> recorded = inboxRepository.find(InboxRecords.INBOUND_COMMAND_CONSUMER,
                cmd.topic(), cmd.eventId());
        if (recorded.isPresent()) {
            return ConsumeResultDto.duplicate("事件[" + cmd.eventId() + "]已处理，返回已记录结果");
        }
        if (isBlank(cmd.requestId()) || isBlank(cmd.sourceSystem()) || isBlank(cmd.sourceOrderNum())
                || isBlank(cmd.warehouseCode())
                || cmd.vins() == null || cmd.vins().isEmpty()
                || cmd.vins().stream().anyMatch(this::isBlank)
                || cmd.vins().stream().distinct().count() != cmd.vins().size()) {
            inboxRepository.insert(InboxRecords.deadLettered(InboxRecords.INBOUND_COMMAND_CONSUMER, cmd.topic(),
                    cmd.eventId(), "VehicleInboundRequested", cmd.payloadDigest(), cmd.sourceOrderNum(),
                    WmsErrorCode.MESSAGE_INVALID.getCode(), "命令字段缺失、仓库缺失或 VIN 列表非法"));
            return ConsumeResultDto.deadLetter("命令结构非法，进入死信");
        }
        try {
            storageHierarchyValidator.requireEnabledWarehouse(cmd.warehouseCode());
        } catch (WmsBusinessException e) {
            return rejectCommand(cmd, null, WmsErrorCode.INBOUND_WAREHOUSE_INVALID, List.of());
        }

        Optional<InboundTask> byRequest = inboundTaskRepository.findBySourceRequest(cmd.sourceSystem(),
                cmd.requestId());
        if (byRequest.isPresent()) {
            return handleExistingCommand(cmd, byRequest.get());
        }

        List<String> sortedVins = cmd.vins().stream().sorted().toList();
        Map<String, VehicleInventory> inboundable = new LinkedHashMap<>();
        List<ItemReasonDto> reasons = new ArrayList<>();
        for (String vin : sortedVins) {
            Optional<VehicleInventory> inventoryOpt = inventoryRepository.findByVin(vin);
            if (inventoryOpt.isEmpty()) {
                reasons.add(new ItemReasonDto(vin, WmsErrorCode.VIN_NOT_INBOUNDABLE.getCode()));
                continue;
            }
            VehicleInventory inventory = inventoryOpt.get();
            if (inventory.getState() != InventoryState.OUTBOUND) {
                reasons.add(new ItemReasonDto(vin, WmsErrorCode.VIN_NOT_INBOUNDABLE.getCode()));
                continue;
            }
            inboundable.put(vin, inventory);
        }
        if (!reasons.isEmpty()) {
            return rejectCommand(cmd, null, WmsErrorCode.INBOUND_TASK_STATE_CONFLICT, reasons);
        }

        Date now = new Date();
        Date estimatedArrivalTime = null;
        List<InboundTask> tasks = new ArrayList<>(sortedVins.size());
        for (String vin : sortedVins) {
            VehicleInventory inventory = inboundable.get(vin);
            InboundTask task = InboundTask.createPending(OrderNumGenerator.generate(
                            OrderNumGenerator.INBOUND_TASK_PREFIX), cmd.requestId(), cmd.sourceSystem(),
                    cmd.sourceOrderNum(), vin, inventory.getBuildConfigCode(), cmd.warehouseCode(),
                    estimatedArrivalTime,
                    inventory.getLifecycleSeq() == null ? 1 : inventory.getLifecycleSeq() + 1, now);
            tasks.add(task);
        }
        try {
            for (InboundTask task : tasks) {
                inboundTaskRepository.insert(task);
            }
        } catch (DuplicateKeyException e) {
            Optional<InboundTask> concurrent = inboundTaskRepository.findBySourceRequest(cmd.sourceSystem(),
                    cmd.requestId());
            if (concurrent.isPresent()) {
                return handleExistingCommand(cmd, concurrent.get());
            }
            throw e;
        }
        for (String vin : sortedVins) {
            if (inventoryRepository.markInTransit(vin, cmd.warehouseCode()) == 0) {
                throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                        "车辆[" + vin + "]进入待入库发生并发冲突，整单回滚");
            }
        }
        for (InboundTask task : tasks) {
            inboundFactPublisher.publishChanged(task, cmd.requestId());
        }
        inboxRepository.insert(InboxRecords.processed(InboxRecords.INBOUND_COMMAND_CONSUMER, cmd.topic(),
                cmd.eventId(), "VehicleInboundRequested", cmd.payloadDigest(), cmd.sourceOrderNum(), null));
        log.info("到货请求[{}]受理，生成待入库任务{}条，目标交付库[{}]", cmd.requestId(), tasks.size(),
                cmd.warehouseCode());
        return ConsumeResultDto.processed("PENDING");
    }

    /**
     * 消费到货请求取消命令
     *
     * @param cmd 取消命令
     * @return 消费结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResultDto consumeCancellation(VehicleInboundCancellationCmd cmd) {
        Optional<InboxRecord> recorded = inboxRepository.find(InboxRecords.INBOUND_COMMAND_CONSUMER,
                cmd.topic(), cmd.eventId());
        if (recorded.isPresent()) {
            return ConsumeResultDto.duplicate("事件[" + cmd.eventId() + "]已处理，返回已记录结果");
        }
        if (isBlank(cmd.requestId()) || isBlank(cmd.sourceSystem()) || isBlank(cmd.sourceOrderNum())) {
            inboxRepository.insert(InboxRecords.deadLettered(InboxRecords.INBOUND_COMMAND_CONSUMER, cmd.topic(),
                    cmd.eventId(), "VehicleInboundCancellationRequested", cmd.payloadDigest(),
                    cmd.sourceOrderNum(), WmsErrorCode.MESSAGE_INVALID.getCode(), "取消命令字段缺失"));
            return ConsumeResultDto.deadLetter("取消命令结构非法，进入死信");
        }
        List<InboundTask> tasks = inboundTaskRepository.search(new InboundTaskCriteria(null, cmd.sourceOrderNum(),
                cmd.sourceSystem(), null, null, null, null, null));
        List<InboundTask> cancellable = tasks.stream()
                .filter(task -> task.getState() == InboundTaskState.PENDING
                        || task.getState() == InboundTaskState.ARRIVED)
                .toList();
        if (cancellable.isEmpty()) {
            String reasonCode = WmsErrorCode.INBOUND_TASK_STATE_CONFLICT.getCode();
            inboundFactPublisher.publishRejected(cmd.requestId(), cmd.sourceSystem(), cmd.sourceOrderNum(),
                    null, reasonCode, List.of(), 0L);
            inboxRepository.insert(InboxRecords.businessRejected(InboxRecords.INBOUND_COMMAND_CONSUMER,
                    cmd.topic(), cmd.eventId(), "VehicleInboundCancellationRequested", cmd.payloadDigest(),
                    cmd.sourceOrderNum(), null, reasonCode));
            return ConsumeResultDto.rejected("无可取消的待入库任务，已发布拒绝事实");
        }
        Date now = new Date();
        for (InboundTask task : cancellable) {
            task.cancel(cmd.operator(), cmd.reason(), now);
            if (inboundTaskRepository.update(task, task.getRowVersion(), cmd.operator()) == 0) {
                throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                        "任务[" + task.getOrderNum() + "]取消失败，已被并发修改");
            }
            if (inventoryRepository.releaseInTransit(task.getVin()) == 0) {
                throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                        "车辆[" + task.getVin() + "]取消待入库发生并发冲突");
            }
            inboundFactPublisher.publishChanged(task, cmd.requestId());
        }
        inboxRepository.insert(InboxRecords.processed(InboxRecords.INBOUND_COMMAND_CONSUMER, cmd.topic(),
                cmd.eventId(), "VehicleInboundCancellationRequested", cmd.payloadDigest(), cmd.sourceOrderNum(),
                null));
        log.info("到货请求[{}]取消，回退待入库任务{}条", cmd.requestId(), cancellable.size());
        return ConsumeResultDto.processed("CANCELLED");
    }

    /**
     * 到货确认
     *
     * @param cmd 到货命令
     * @return 任务
     */
    @Transactional(rollbackFor = Exception.class)
    public InboundTask arrival(ArrivalInboundTaskCmd cmd) {
        InboundTask task = requireTask(cmd.orderNum());
        if (mptActionGuard.alreadyProcessed(ACTION_ARRIVAL, cmd.requestId())
                || task.getState() == InboundTaskState.ARRIVED) {
            return task;
        }
        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : task.getRowVersion();
        task.arrival(cmd.operator(), new Date());
        if (inboundTaskRepository.update(task, expectedVersion, cmd.operator()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "任务[" + cmd.orderNum() + "]到货确认发生并发冲突");
        }
        inboundFactPublisher.publishChanged(task, cmd.requestId());
        mptActionGuard.recordProcessed(ACTION_ARRIVAL, cmd.requestId(), cmd.orderNum());
        log.info("任务[{}]车辆[{}]到货确认，目标交付库[{}]", cmd.orderNum(), task.getVin(), task.getWarehouseCode());
        return task;
    }

    /**
     * 入库确认
     *
     * @param cmd 入库命令
     * @return 任务
     */
    @Transactional(rollbackFor = Exception.class)
    public InboundTask confirmInbound(ConfirmInboundTaskCmd cmd) {
        InboundTask task = requireTask(cmd.orderNum());
        if (mptActionGuard.alreadyProcessed(ACTION_INBOUND, cmd.requestId())
                || task.getState() == InboundTaskState.COMPLETED) {
            return task;
        }
        if (task.getState() != InboundTaskState.ARRIVED) {
            throw WmsBusinessException.of(WmsErrorCode.INBOUND_TASK_STATE_CONFLICT,
                    "任务[" + cmd.orderNum() + "]当前状态为[" + task.getState() + "]，不允许入库");
        }
        storageHierarchyValidator.requireEnabledLocation(task.getWarehouseCode(), cmd.storageAreaCode(),
                cmd.storageLocationCode());
        if (inventoryRepository.isLocationOccupied(task.getWarehouseCode(), cmd.storageLocationCode())) {
            throw WmsBusinessException.of(WmsErrorCode.STORAGE_LOCATION_OCCUPIED,
                    "仓库[" + task.getWarehouseCode() + "]储位[" + cmd.storageLocationCode() + "]已被占用");
        }
        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : task.getRowVersion();
        Date now = new Date();
        InboundOrderFact fact = InboundOrderFact.builder()
                .orderNum(OrderNumGenerator.generate(OrderNumGenerator.INBOUND_PREFIX))
                .preOrderNum(task.getOrderNum())
                .lifecycleSeq(task.getLifecycleSeq())
                .vin(task.getVin())
                .buildConfigCode(task.getBuildConfigCode())
                .warehouseCode(task.getWarehouseCode())
                .storageAreaCode(cmd.storageAreaCode())
                .storageLocationCode(cmd.storageLocationCode())
                .inboundTime(now)
                .inboundBy(cmd.operator())
                .build();
        try {
            inboundOrderRepository.insert(fact);
        } catch (DuplicateKeyException e) {
            throw WmsBusinessException.of(WmsErrorCode.INBOUND_TASK_STATE_CONFLICT,
                    "任务[" + cmd.orderNum() + "]入库事实已存在");
        }
        if (inventoryRepository.confirmInbound(task.getVin(), task.getWarehouseCode(), cmd.storageAreaCode(),
                cmd.storageLocationCode(), cmd.operator(), now) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.VIN_NOT_INBOUNDABLE,
                    "车辆[" + task.getVin() + "]待入库信息与任务不一致");
        }
        task.inbound(cmd.operator(), now);
        if (inboundTaskRepository.update(task, expectedVersion, cmd.operator()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "任务[" + cmd.orderNum() + "]入库确认发生并发冲突");
        }
        inboundFactPublisher.publishChanged(task, cmd.requestId());
        mptActionGuard.recordProcessed(ACTION_INBOUND, cmd.requestId(), cmd.orderNum());
        log.info("任务[{}]车辆[{}]完成交付库入库，储位[{}/{}]", cmd.orderNum(), task.getVin(),
                cmd.storageAreaCode(), cmd.storageLocationCode());
        return task;
    }

    /**
     * 取消待入库任务
     *
     * @param cmd 取消命令
     * @return 任务
     */
    @Transactional(rollbackFor = Exception.class)
    public InboundTask cancel(CancelInboundTaskCmd cmd) {
        InboundTask task = requireTask(cmd.orderNum());
        if (mptActionGuard.alreadyProcessed(ACTION_CANCEL, cmd.requestId())
                || task.getState() == InboundTaskState.CANCELLED) {
            return task;
        }
        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : task.getRowVersion();
        task.cancel(cmd.operator(), cmd.reason(), new Date());
        if (inboundTaskRepository.update(task, expectedVersion, cmd.operator()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "任务[" + cmd.orderNum() + "]取消发生并发冲突");
        }
        if (inventoryRepository.releaseInTransit(task.getVin()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "车辆[" + task.getVin() + "]取消待入库发生并发冲突");
        }
        inboundFactPublisher.publishChanged(task, cmd.requestId());
        mptActionGuard.recordProcessed(ACTION_CANCEL, cmd.requestId(), cmd.orderNum());
        log.info("任务[{}]车辆[{}]取消，库存回退为已出库", cmd.orderNum(), task.getVin());
        return task;
    }

    private ConsumeResultDto handleExistingCommand(VehicleInboundRequestedCmd cmd, InboundTask task) {
        boolean equivalent = task.getSourceOrderNum().equals(cmd.sourceOrderNum())
                && task.getWarehouseCode().equals(cmd.warehouseCode())
                && cmd.vins().contains(task.getVin());
        if (!equivalent) {
            inboxRepository.insert(InboxRecords.deadLettered(InboxRecords.INBOUND_COMMAND_CONSUMER, cmd.topic(),
                    cmd.eventId(), "VehicleInboundRequested", cmd.payloadDigest(), cmd.sourceOrderNum(),
                    WmsErrorCode.REQUEST_ID_CONFLICT.getCode(), "请求标识被不同内容复用"));
            return ConsumeResultDto.deadLetter("请求标识被不同内容复用，进入死信");
        }
        inboxRepository.insert(InboxRecords.processed(InboxRecords.INBOUND_COMMAND_CONSUMER, cmd.topic(),
                cmd.eventId(), "VehicleInboundRequested", cmd.payloadDigest(), task.getOrderNum(), null));
        log.info("到货请求[{}]重复投递，复用任务[{}]", cmd.requestId(), task.getOrderNum());
        return ConsumeResultDto.duplicate("任务[" + task.getOrderNum() + "]已存在，返回原结果");
    }

    private ConsumeResultDto rejectCommand(VehicleInboundRequestedCmd cmd, String inboundTaskNum,
                                           WmsErrorCode reasonCode, List<ItemReasonDto> itemReasons) {
        inboundFactPublisher.publishRejected(cmd.requestId(), cmd.sourceSystem(), cmd.sourceOrderNum(),
                inboundTaskNum, reasonCode.getCode(), itemReasons, 0L);
        inboxRepository.insert(InboxRecords.businessRejected(InboxRecords.INBOUND_COMMAND_CONSUMER, cmd.topic(),
                cmd.eventId(), "VehicleInboundRequested", cmd.payloadDigest(), cmd.sourceOrderNum(), null,
                reasonCode.getCode()));
        log.warn("到货请求[{}]被拒绝，原因[{}]，逐 VIN 原因{}条", cmd.requestId(), reasonCode.getCode(),
                itemReasons.size());
        return ConsumeResultDto.rejected(reasonCode.getMessage());
    }

    private InboundTask requireTask(String orderNum) {
        return inboundTaskRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> WmsBusinessException.of(WmsErrorCode.INBOUND_TASK_STATE_CONFLICT,
                        "任务[" + orderNum + "]不存在"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
