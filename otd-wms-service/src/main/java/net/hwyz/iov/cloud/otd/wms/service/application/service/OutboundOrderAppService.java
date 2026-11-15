package net.hwyz.iov.cloud.otd.wms.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.InboxRecords;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CancelOutboundOrderCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.LoadVinCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.OutboundVinCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleOutboundCancellationCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleOutboundRequestedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ItemReasonDto;
import net.hwyz.iov.cloud.otd.wms.service.application.port.OutboundFactPublisher;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundOrderState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.common.util.OrderNumGenerator;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.OutboundOrderCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;
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
 * 出库应用服务
 * <p>
 * 通用命令整单原子预留、按 VIN 装车／出库、装车前取消；每次接受的动作以本地事务
 * 写入明细、库存、订单状态与 Outbox 事实。
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundOrderAppService {

    private static final String ACTION_LOAD = "outboundOrder.loading";
    private static final String ACTION_OUTBOUND = "outboundOrder.outbound";
    private static final String ACTION_CANCEL = "outboundOrder.cancellation";

    private final OutboundOrderRepository outboundOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final InboxRepository inboxRepository;
    private final OutboundFactPublisher outboundFactPublisher;
    private final WarehouseResolver warehouseResolver;
    private final MptActionGuard mptActionGuard;

    /**
     * 查询出库单
     *
     * @param criteria 查询条件
     * @return 出库单列表
     */
    public List<OutboundOrder> search(OutboundOrderCriteria criteria) {
        return outboundOrderRepository.search(criteria);
    }

    /**
     * 按主键查询出库单（含明细）
     *
     * @param id 主键
     * @return 出库单
     */
    public OutboundOrder getById(Long id) {
        return outboundOrderRepository.findById(id).orElse(null);
    }

    /**
     * 查询出库单 VIN 明细
     *
     * @param id 出库单主键
     * @return 明细列表
     */
    public List<OutboundOrderDetail> listDetails(Long id) {
        OutboundOrder order = outboundOrderRepository.findById(id).orElse(null);
        return order == null ? List.of() : order.getDetails();
    }

    // ==================== Kafka 通用命令 ====================

    /**
     * 消费通用出库请求命令
     *
     * @param cmd 出库命令
     * @return 消费结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResultDto consumeRequested(VehicleOutboundRequestedCmd cmd) {
        Optional<InboxRecord> recorded = inboxRepository.find(InboxRecords.OUTBOUND_COMMAND_CONSUMER,
                cmd.topic(), cmd.eventId());
        if (recorded.isPresent()) {
            return ConsumeResultDto.duplicate("事件[" + cmd.eventId() + "]已处理，返回已记录结果");
        }
        if (isBlank(cmd.requestId()) || isBlank(cmd.sourceSystem()) || isBlank(cmd.sourceOrderNum())
                || cmd.vins() == null || cmd.vins().isEmpty()
                || cmd.vins().stream().anyMatch(this::isBlank)
                || cmd.vins().stream().distinct().count() != cmd.vins().size()) {
            inboxRepository.insert(InboxRecords.deadLettered(InboxRecords.OUTBOUND_COMMAND_CONSUMER, cmd.topic(),
                    cmd.eventId(), "VehicleOutboundRequested", cmd.payloadDigest(), cmd.sourceOrderNum(),
                    WmsErrorCode.MESSAGE_INVALID.getCode(), "命令字段缺失或 VIN 列表非法"));
            return ConsumeResultDto.deadLetter("命令结构非法，进入死信");
        }

        String warehouseCode;
        try {
            warehouseCode = warehouseResolver.resolve(cmd.warehouseCode());
        } catch (WmsBusinessException e) {
            return rejectCommand(cmd, null, WmsErrorCode.WAREHOUSE_INVALID, List.of());
        }

        Optional<OutboundOrder> byRequest = outboundOrderRepository.findBySourceRequest(cmd.sourceSystem(),
                cmd.requestId());
        if (byRequest.isPresent()) {
            return handleExistingCommand(cmd, byRequest.get());
        }
        Optional<OutboundOrder> bySourceOrder = outboundOrderRepository.findBySourceOrder(cmd.sourceSystem(),
                cmd.sourceOrderNum());
        if (bySourceOrder.isPresent()) {
            return handleExistingCommand(cmd, bySourceOrder.get());
        }

        List<String> sortedVins = cmd.vins().stream().sorted().toList();
        Map<String, VehicleInventory> reservable = new LinkedHashMap<>();
        List<ItemReasonDto> reasons = new ArrayList<>();
        for (String vin : sortedVins) {
            Optional<VehicleInventory> inventoryOpt = inventoryRepository.findByVin(vin);
            if (inventoryOpt.isEmpty()) {
                reasons.add(new ItemReasonDto(vin, WmsErrorCode.VIN_NOT_AVAILABLE.getCode()));
                continue;
            }
            VehicleInventory inventory = inventoryOpt.get();
            if (!warehouseCode.equals(inventory.getWarehouseCode())
                    || inventory.getState() != InventoryState.AVAILABLE) {
                reasons.add(new ItemReasonDto(vin, WmsErrorCode.VIN_NOT_AVAILABLE.getCode()));
                continue;
            }
            reservable.put(vin, inventory);
        }
        if (!reasons.isEmpty()) {
            return rejectCommand(cmd, null, WmsErrorCode.OUTBOUND_RESERVATION_REJECTED, reasons);
        }

        Date now = new Date();
        String orderNum = OrderNumGenerator.generate(OrderNumGenerator.OUTBOUND_PREFIX);
        List<OutboundOrderDetail> details = new ArrayList<>(sortedVins.size());
        for (String vin : sortedVins) {
            VehicleInventory inventory = reservable.get(vin);
            details.add(OutboundOrderDetail.builder()
                    .orderNum(orderNum)
                    .vin(vin)
                    .buildConfigCode(inventory.getBuildConfigCode())
                    .warehouseCode(inventory.getWarehouseCode())
                    .storageAreaCode(inventory.getStorageAreaCode())
                    .storageLocationCode(inventory.getStorageLocationCode())
                    .state(net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundDetailState.RESERVED)
                    .reserveTime(now)
                    .reserveBy(cmd.operator())
                    .rowVersion(1)
                    .build());
        }
        OutboundOrder order = OutboundOrder.createReserved(cmd.requestId(), cmd.sourceSystem(),
                cmd.sourceOrderNum(), warehouseCode, orderNum, details);
        try {
            outboundOrderRepository.insert(order);
        } catch (DuplicateKeyException e) {
            Optional<OutboundOrder> concurrent = outboundOrderRepository.findBySourceRequest(cmd.sourceSystem(),
                    cmd.requestId());
            if (concurrent.isPresent()) {
                return handleExistingCommand(cmd, concurrent.get());
            }
            throw e;
        }
        for (String vin : sortedVins) {
            if (inventoryRepository.reserve(vin, orderNum) == 0) {
                // 并发导致库存状态变化：整单回滚，交由有界重试重新读取收敛
                throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                        "车辆[" + vin + "]预留发生并发冲突，整单回滚");
            }
        }
        outboundFactPublisher.publishChanged(order, cmd.requestId());
        inboxRepository.insert(InboxRecords.processed(InboxRecords.OUTBOUND_COMMAND_CONSUMER, cmd.topic(),
                cmd.eventId(), "VehicleOutboundRequested", cmd.payloadDigest(), orderNum, null));
        logger.info("出库命令[{}]创建出库单[{}]，预留车辆{}台", cmd.requestId(), orderNum, sortedVins.size());
        return ConsumeResultDto.processed("RESERVED");
    }

    /**
     * 消费通用出库取消命令
     *
     * @param cmd 取消命令
     * @return 消费结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResultDto consumeCancellation(VehicleOutboundCancellationCmd cmd) {
        Optional<InboxRecord> recorded = inboxRepository.find(InboxRecords.OUTBOUND_COMMAND_CONSUMER,
                cmd.topic(), cmd.eventId());
        if (recorded.isPresent()) {
            return ConsumeResultDto.duplicate("事件[" + cmd.eventId() + "]已处理，返回已记录结果");
        }
        if (isBlank(cmd.requestId()) || isBlank(cmd.sourceSystem()) || isBlank(cmd.sourceOrderNum())) {
            inboxRepository.insert(InboxRecords.deadLettered(InboxRecords.OUTBOUND_COMMAND_CONSUMER, cmd.topic(),
                    cmd.eventId(), "VehicleOutboundCancellationRequested", cmd.payloadDigest(),
                    cmd.sourceOrderNum(), WmsErrorCode.MESSAGE_INVALID.getCode(), "取消命令字段缺失"));
            return ConsumeResultDto.deadLetter("取消命令结构非法，进入死信");
        }

        Optional<OutboundOrder> orderOpt = outboundOrderRepository.findBySourceOrder(cmd.sourceSystem(),
                cmd.sourceOrderNum());
        if (orderOpt.isEmpty()) {
            String reasonCode = WmsErrorCode.OUTBOUND_STATE_CONFLICT.getCode();
            outboundFactPublisher.publishRejected(cmd.requestId(), cmd.sourceSystem(), cmd.sourceOrderNum(),
                    null, reasonCode, List.of(), 0L);
            inboxRepository.insert(InboxRecords.businessRejected(InboxRecords.OUTBOUND_COMMAND_CONSUMER,
                    cmd.topic(), cmd.eventId(), "VehicleOutboundCancellationRequested", cmd.payloadDigest(),
                    cmd.sourceOrderNum(), null, reasonCode));
            return ConsumeResultDto.rejected("出库单不存在，已发布拒绝事实");
        }
        OutboundOrder order = orderOpt.get();
        if (order.getState() == OutboundOrderState.CANCELLED) {
            inboxRepository.insert(InboxRecords.processed(InboxRecords.OUTBOUND_COMMAND_CONSUMER, cmd.topic(),
                    cmd.eventId(), "VehicleOutboundCancellationRequested", cmd.payloadDigest(), order.getOrderNum(),
                    null));
            return ConsumeResultDto.duplicate("出库单[" + order.getOrderNum() + "]已取消");
        }
        try {
            order.cancel(cmd.operator(), new Date());
        } catch (WmsBusinessException e) {
            String reasonCode = e.getErrorCode().getCode();
            outboundFactPublisher.publishRejected(cmd.requestId(), cmd.sourceSystem(), cmd.sourceOrderNum(),
                    order.getOrderNum(), reasonCode, List.of(), order.getAggregateVersion());
            inboxRepository.insert(InboxRecords.businessRejected(InboxRecords.OUTBOUND_COMMAND_CONSUMER,
                    cmd.topic(), cmd.eventId(), "VehicleOutboundCancellationRequested", cmd.payloadDigest(),
                    order.getOrderNum(), null, reasonCode));
            return ConsumeResultDto.rejected("出库单[" + order.getOrderNum() + "]已装车或已出库，拒绝取消");
        }
        releaseAndCancel(order, cmd.requestId(), cmd.operator());
        inboxRepository.insert(InboxRecords.processed(InboxRecords.OUTBOUND_COMMAND_CONSUMER, cmd.topic(),
                cmd.eventId(), "VehicleOutboundCancellationRequested", cmd.payloadDigest(), order.getOrderNum(),
                null));
        logger.info("出库单[{}]按命令[{}]取消，释放车辆{}台", order.getOrderNum(), cmd.requestId(),
                order.getDetails().size());
        return ConsumeResultDto.processed("CANCELLED");
    }

    /**
     * 查询已记录的命令处理结果
     *
     * @param topic   来源 Topic
     * @param eventId 事件 ID
     * @return 已记录结果
     */
    public Optional<InboxRecord> findRecordedCommandResult(String topic, String eventId) {
        return inboxRepository.find(InboxRecords.OUTBOUND_COMMAND_CONSUMER, topic, eventId);
    }

    // ==================== MPT 业务动作 ====================

    /**
     * 按 VIN 确认装车
     *
     * @param cmd 装车命令
     * @return 流转后的明细
     */
    @Transactional(rollbackFor = Exception.class)
    public OutboundOrderDetail loadVin(LoadVinCmd cmd) {
        OutboundOrder order = requireOrder(cmd.orderNum());
        OutboundOrderDetail current = requireDetail(order, cmd.vin());
        if (mptActionGuard.alreadyProcessed(ACTION_LOAD, cmd.requestId())) {
            return current;
        }
        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : current.getRowVersion();
        int orderExpectedVersion = order.getRowVersion();
        OutboundOrderDetail detail = order.loadVin(cmd.vin(), cmd.operator(), new Date());
        if (outboundOrderRepository.updateDetail(detail, expectedVersion, cmd.operator()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "车辆[" + cmd.vin() + "]装车发生并发冲突");
        }
        if (outboundOrderRepository.updateOrder(order, orderExpectedVersion, cmd.operator()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "出库单[" + cmd.orderNum() + "]装车发生并发冲突");
        }
        outboundFactPublisher.publishChanged(order, cmd.requestId());
        mptActionGuard.recordProcessed(ACTION_LOAD, cmd.requestId(), cmd.orderNum() + "/" + cmd.vin());
        logger.info("出库单[{}]车辆[{}]确认装车，订单状态[{}]", cmd.orderNum(), cmd.vin(), order.getState());
        return detail;
    }

    /**
     * 按 VIN 确认出库
     *
     * @param cmd 出库命令
     * @return 流转后的明细
     */
    @Transactional(rollbackFor = Exception.class)
    public OutboundOrderDetail outboundVin(OutboundVinCmd cmd) {
        OutboundOrder order = requireOrder(cmd.orderNum());
        OutboundOrderDetail current = requireDetail(order, cmd.vin());
        if (mptActionGuard.alreadyProcessed(ACTION_OUTBOUND, cmd.requestId())) {
            return current;
        }
        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : current.getRowVersion();
        int orderExpectedVersion = order.getRowVersion();
        OutboundOrderDetail detail = order.outboundVin(cmd.vin(), cmd.operator(), new Date());
        if (inventoryRepository.markOutbound(cmd.vin(), cmd.orderNum(), cmd.operator(), new Date()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.VIN_NOT_AVAILABLE,
                    "车辆[" + cmd.vin() + "]库存预留信息与出库单不一致");
        }
        if (outboundOrderRepository.updateDetail(detail, expectedVersion, cmd.operator()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "车辆[" + cmd.vin() + "]出库发生并发冲突");
        }
        if (outboundOrderRepository.updateOrder(order, orderExpectedVersion, cmd.operator()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "出库单[" + cmd.orderNum() + "]出库发生并发冲突");
        }
        outboundFactPublisher.publishChanged(order, cmd.requestId());
        mptActionGuard.recordProcessed(ACTION_OUTBOUND, cmd.requestId(), cmd.orderNum() + "/" + cmd.vin());
        logger.info("出库单[{}]车辆[{}]确认出库，订单状态[{}]", cmd.orderNum(), cmd.vin(), order.getState());
        return detail;
    }

    /**
     * 取消出库单（装车前）
     *
     * @param cmd 取消命令
     * @return 出库单
     */
    @Transactional(rollbackFor = Exception.class)
    public OutboundOrder cancel(CancelOutboundOrderCmd cmd) {
        OutboundOrder order = requireOrder(cmd.orderNum());
        if (mptActionGuard.alreadyProcessed(ACTION_CANCEL, cmd.requestId())
                || order.getState() == OutboundOrderState.CANCELLED) {
            return order;
        }
        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : order.getRowVersion();
        order.cancel(cmd.operator(), new Date());
        if (outboundOrderRepository.updateOrder(order, expectedVersion, cmd.operator()) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "出库单[" + cmd.orderNum() + "]取消发生并发冲突");
        }
        for (OutboundOrderDetail detail : order.getDetails()) {
            if (inventoryRepository.release(detail.getVin(), order.getOrderNum()) == 0) {
                throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                        "车辆[" + detail.getVin() + "]取消预留发生并发冲突");
            }
            if (outboundOrderRepository.updateDetail(detail, detail.getRowVersion(), cmd.operator()) == 0) {
                throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                        "车辆[" + detail.getVin() + "]取消释放发生并发冲突");
            }
        }
        outboundFactPublisher.publishChanged(order, cmd.requestId());
        mptActionGuard.recordProcessed(ACTION_CANCEL, cmd.requestId(), cmd.orderNum());
        logger.info("出库单[{}]取消，释放车辆{}台", cmd.orderNum(), order.getDetails().size());
        return order;
    }

    // ==================== 内部实现 ====================

    private ConsumeResultDto handleExistingCommand(VehicleOutboundRequestedCmd cmd, OutboundOrder order) {
        boolean equivalent = order.getWarehouseCode().equals(cmd.warehouseCode() == null || cmd.warehouseCode().isBlank()
                ? order.getWarehouseCode() : cmd.warehouseCode())
                && order.getSourceOrderNum().equals(cmd.sourceOrderNum())
                && order.getDetails().size() == cmd.vins().size()
                && order.getDetails().stream().map(OutboundOrderDetail::getVin).sorted().toList()
                .equals(cmd.vins().stream().sorted().toList());
        if (!equivalent) {
            inboxRepository.insert(InboxRecords.deadLettered(InboxRecords.OUTBOUND_COMMAND_CONSUMER, cmd.topic(),
                    cmd.eventId(), "VehicleOutboundRequested", cmd.payloadDigest(), cmd.sourceOrderNum(),
                    WmsErrorCode.REQUEST_ID_CONFLICT.getCode(), "请求标识或来源单号被不同内容复用"));
            return ConsumeResultDto.deadLetter("请求标识或来源单号被不同内容复用，进入死信");
        }
        inboxRepository.insert(InboxRecords.processed(InboxRecords.OUTBOUND_COMMAND_CONSUMER, cmd.topic(),
                cmd.eventId(), "VehicleOutboundRequested", cmd.payloadDigest(), order.getOrderNum(), null));
        logger.info("出库命令[{}]重复投递，复用出库单[{}]", cmd.requestId(), order.getOrderNum());
        return ConsumeResultDto.duplicate("出库单[" + order.getOrderNum() + "]已存在，返回原结果");
    }

    private ConsumeResultDto rejectCommand(VehicleOutboundRequestedCmd cmd, String outboundOrderNum,
                                           WmsErrorCode reasonCode, List<ItemReasonDto> itemReasons) {
        outboundFactPublisher.publishRejected(cmd.requestId(), cmd.sourceSystem(), cmd.sourceOrderNum(),
                outboundOrderNum, reasonCode.getCode(), itemReasons, 0L);
        inboxRepository.insert(InboxRecords.businessRejected(InboxRecords.OUTBOUND_COMMAND_CONSUMER, cmd.topic(),
                cmd.eventId(), "VehicleOutboundRequested", cmd.payloadDigest(), cmd.sourceOrderNum(), null,
                reasonCode.getCode()));
        logger.warn("出库命令[{}]被拒绝，原因[{}]，逐 VIN 原因{}条", cmd.requestId(), reasonCode.getCode(),
                itemReasons.size());
        return ConsumeResultDto.rejected(reasonCode.getMessage());
    }

    private void releaseAndCancel(OutboundOrder order, String requestId, String operator) {
        for (OutboundOrderDetail detail : order.getDetails()) {
            if (inventoryRepository.release(detail.getVin(), order.getOrderNum()) == 0) {
                throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                        "车辆[" + detail.getVin() + "]取消预留发生并发冲突");
            }
        }
        for (OutboundOrderDetail detail : order.getDetails()) {
            if (outboundOrderRepository.updateDetail(detail, detail.getRowVersion(), operator) == 0) {
                throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                        "车辆[" + detail.getVin() + "]取消释放发生并发冲突");
            }
        }
        if (outboundOrderRepository.updateOrder(order, order.getRowVersion(), operator) == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "出库单[" + order.getOrderNum() + "]取消失败，已被并发修改");
        }
        outboundFactPublisher.publishChanged(order, requestId);
    }

    private OutboundOrder requireOrder(String orderNum) {
        return outboundOrderRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> WmsBusinessException.of(WmsErrorCode.OUTBOUND_STATE_CONFLICT,
                        "出库单[" + orderNum + "]不存在"));
    }

    private OutboundOrderDetail requireDetail(OutboundOrder order, String vin) {
        return order.getDetails().stream()
                .filter(detail -> detail.getVin().equals(vin))
                .findFirst()
                .orElseThrow(() -> WmsBusinessException.of(WmsErrorCode.OUTBOUND_STATE_CONFLICT,
                        "出库单[" + order.getOrderNum() + "]不存在车辆[" + vin + "]明细"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
