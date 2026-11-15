package net.hwyz.iov.cloud.otd.wms.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CancelPreInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmArrivalCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.PreInboundState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.common.util.OrderNumGenerator;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboundOrderFact;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InboundOrderCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.PreInboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.service.StorageHierarchyValidator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 入库应用服务
 * <p>
 * 到达、入库与取消为显式业务动作：{@code state} 是唯一权威，入库事实与库存占用在同一本地事务内完成。
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundOrderAppService {

    private static final String ACTION_ARRIVAL = "preInboundOrder.arrival";
    private static final String ACTION_INBOUND = "preInboundOrder.inbound";
    private static final String ACTION_CANCEL = "preInboundOrder.cancellation";

    private final PreInboundOrderRepository preInboundOrderRepository;
    private final InboundOrderRepository inboundOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final StorageHierarchyValidator storageHierarchyValidator;
    private final MptActionGuard mptActionGuard;

    /**
     * 查询入库单
     *
     * @param criteria 查询条件
     * @return 入库单列表
     */
    public List<InboundOrderFact> search(InboundOrderCriteria criteria) {
        return inboundOrderRepository.search(criteria);
    }

    /**
     * 按主键查询入库单
     *
     * @param id 主键
     * @return 入库单
     */
    public InboundOrderFact getById(Long id) {
        return inboundOrderRepository.findById(id).orElse(null);
    }

    /**
     * 确认到达
     *
     * @param cmd 到达命令
     * @return 预入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public PreInboundOrder confirmArrival(ConfirmArrivalCmd cmd) {
        PreInboundOrder order = requireOrder(cmd.orderNum());
        if (mptActionGuard.alreadyProcessed(ACTION_ARRIVAL, cmd.requestId())
                || order.getState() == PreInboundState.ARRIVED) {
            return order;
        }
        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : order.getRowVersion();
        order.confirmArrival(new Date());
        int rows = preInboundOrderRepository.updateState(order, expectedVersion,
                PreInboundState.PENDING_ARRIVAL.name(), cmd.operator());
        if (rows == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "预入库单[" + cmd.orderNum() + "]到达确认发生并发冲突或状态不符");
        }
        mptActionGuard.recordProcessed(ACTION_ARRIVAL, cmd.requestId(), cmd.orderNum());
        log.info("车辆[{}]预入库单[{}]确认到达", order.getVin(), order.getOrderNum());
        return order;
    }

    /**
     * 确认入库
     * <p>
     * 原子完成：入库事实 + AVAILABLE 库存（占用储位）+ 预入库 ARRIVED → COMPLETED。
     *
     * @param cmd 入库命令
     * @return 预入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public PreInboundOrder confirmInbound(ConfirmInboundCmd cmd) {
        PreInboundOrder order = requireOrder(cmd.orderNum());
        if (mptActionGuard.alreadyProcessed(ACTION_INBOUND, cmd.requestId())
                || order.getState() == PreInboundState.COMPLETED) {
            return order;
        }
        if (!order.isArrived()) {
            throw WmsBusinessException.of(WmsErrorCode.PRE_INBOUND_STATE_CONFLICT,
                    "预入库单[" + cmd.orderNum() + "]当前状态为[" + order.getState() + "]，不允许入库");
        }
        storageHierarchyValidator.requireEnabledLocation(order.getWarehouseCode(),
                cmd.storageAreaCode(), cmd.storageLocationCode());
        if (inventoryRepository.isLocationOccupied(order.getWarehouseCode(), cmd.storageLocationCode())) {
            throw WmsBusinessException.of(WmsErrorCode.STORAGE_LOCATION_OCCUPIED,
                    "仓库[" + order.getWarehouseCode() + "]储位[" + cmd.storageLocationCode() + "]已被占用");
        }
        if (inventoryRepository.findByVin(order.getVin()).isPresent()) {
            throw WmsBusinessException.of(WmsErrorCode.VIN_NOT_AVAILABLE,
                    "车辆[" + order.getVin() + "]已存在当前库存");
        }

        Date now = new Date();
        InboundOrderFact fact = InboundOrderFact.builder()
                .orderNum(OrderNumGenerator.generate(OrderNumGenerator.INBOUND_PREFIX))
                .preOrderNum(order.getOrderNum())
                .vin(order.getVin())
                .buildConfigCode(order.getBuildConfigCode())
                .warehouseCode(order.getWarehouseCode())
                .storageAreaCode(cmd.storageAreaCode())
                .storageLocationCode(cmd.storageLocationCode())
                .inboundTime(now)
                .inboundBy(cmd.operator())
                .build();
        VehicleInventory inventory = VehicleInventory.builder()
                .vin(order.getVin())
                .buildConfigCode(order.getBuildConfigCode())
                .warehouseCode(order.getWarehouseCode())
                .storageAreaCode(cmd.storageAreaCode())
                .storageLocationCode(cmd.storageLocationCode())
                .state(InventoryState.AVAILABLE)
                .inboundTime(now)
                .inboundBy(cmd.operator())
                .rowVersion(1)
                .build();
        try {
            inboundOrderRepository.insert(fact);
            inventoryRepository.insert(inventory);
        } catch (DuplicateKeyException e) {
            throw WmsBusinessException.of(WmsErrorCode.VIN_NOT_AVAILABLE,
                    "车辆[" + order.getVin() + "]入库事实或当前库存已存在");
        }

        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : order.getRowVersion();
        order.confirmInbound(cmd.operator(), now);
        int rows = preInboundOrderRepository.updateState(order, expectedVersion,
                PreInboundState.ARRIVED.name(), cmd.operator());
        if (rows == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "预入库单[" + cmd.orderNum() + "]入库确认发生并发冲突");
        }
        mptActionGuard.recordProcessed(ACTION_INBOUND, cmd.requestId(), cmd.orderNum());
        log.info("车辆[{}]预入库单[{}]完成入库，入库单[{}]，储位[{}/{}]", order.getVin(), order.getOrderNum(),
                fact.getOrderNum(), cmd.storageAreaCode(), cmd.storageLocationCode());
        return order;
    }

    /**
     * 取消预入库单（不创建库存）
     *
     * @param cmd 取消命令
     * @return 预入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public PreInboundOrder cancelPreInbound(CancelPreInboundCmd cmd) {
        PreInboundOrder order = requireOrder(cmd.orderNum());
        if (mptActionGuard.alreadyProcessed(ACTION_CANCEL, cmd.requestId())
                || order.getState() == PreInboundState.CANCELLED) {
            return order;
        }
        PreInboundState transitFrom = order.getState();
        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : order.getRowVersion();
        order.cancel();
        int rows = preInboundOrderRepository.updateState(order, expectedVersion, transitFrom.name(),
                cmd.operator());
        if (rows == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "预入库单[" + cmd.orderNum() + "]取消发生并发冲突");
        }
        mptActionGuard.recordProcessed(ACTION_CANCEL, cmd.requestId(), cmd.orderNum());
        log.info("预入库单[{}]取消，原因[{}]", cmd.orderNum(), cmd.reason());
        return order;
    }

    private PreInboundOrder requireOrder(String orderNum) {
        return preInboundOrderRepository.findByOrderNum(orderNum)
                .orElseThrow(() -> WmsBusinessException.of(WmsErrorCode.PRE_INBOUND_STATE_CONFLICT,
                        "预入库单[" + orderNum + "]不存在"));
    }
}
