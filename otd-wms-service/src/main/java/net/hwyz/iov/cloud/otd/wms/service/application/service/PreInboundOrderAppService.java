package net.hwyz.iov.cloud.otd.wms.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.InboxRecords;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConsumeVehicleProducedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CreatePreInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.PreInboundState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.common.util.OrderNumGenerator;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.PreInboundOrderCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.PreInboundOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 预入库应用服务
 * <p>
 * 负责 VMD 生产事实的幂等消费与预入库单查询；到达／入库／取消流转见
 * {@link InboundOrderAppService}。
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreInboundOrderAppService {

    /**
     * VMD 生产事实来源系统标识
     */
    private static final String SOURCE_SYSTEM_VMD = "VMD";
    /**
     * 存量同步接口来源系统标识（非 VMD 接入路径）
     */
    private static final String SOURCE_SYSTEM_LEGACY_SYNC = "LEGACY_SYNC";

    private final PreInboundOrderRepository preInboundOrderRepository;
    private final InboxRepository inboxRepository;
    private final WarehouseResolver warehouseResolver;

    /**
     * 查询预入库单
     *
     * @param criteria 查询条件
     * @return 预入库单列表
     */
    public List<PreInboundOrder> search(PreInboundOrderCriteria criteria) {
        return preInboundOrderRepository.search(criteria);
    }

    /**
     * 按主键查询
     *
     * @param id 主键
     * @return 预入库单
     */
    public PreInboundOrder getById(Long id) {
        return preInboundOrderRepository.findById(id).orElse(null);
    }

    /**
     * 按单号查询
     *
     * @param orderNum 预入库单号
     * @return 预入库单
     */
    public PreInboundOrder getByOrderNum(String orderNum) {
        return preInboundOrderRepository.findByOrderNum(orderNum).orElse(null);
    }

    /**
     * 存量同步接口创建预入库单
     * <p>
     * 该入口保留兼容，不是 VMD 集成路径；VMD 只能通过 Kafka 生产事实接入。
     *
     * @param cmd 创建命令
     * @return 预入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public PreInboundOrder createPreInbound(CreatePreInboundCmd cmd) {
        String warehouseCode = warehouseResolver.resolve(cmd.warehouseCode());
        if (preInboundOrderRepository.findByVin(cmd.vin()).isPresent()) {
            throw WmsBusinessException.of(WmsErrorCode.VIN_NOT_AVAILABLE,
                    "车辆[" + cmd.vin() + "]已存在预入库单");
        }
        PreInboundOrder order = PreInboundOrder.builder()
                .orderNum(OrderNumGenerator.generate(OrderNumGenerator.PRE_INBOUND_PREFIX))
                .vin(cmd.vin())
                .buildConfigCode(cmd.buildConfigCode())
                .warehouseCode(warehouseCode)
                .state(PreInboundState.PENDING_ARRIVAL)
                .sourceSystem(SOURCE_SYSTEM_LEGACY_SYNC)
                .sourceEventId(UUID.randomUUID().toString())
                .estimatedArrivalTime(cmd.estimatedArrivalTime())
                .estimatedInboundTime(cmd.estimatedInboundTime())
                .rowVersion(1)
                .build();
        preInboundOrderRepository.insert(order);
        logger.info("存量同步接口创建车辆[{}]预入库单[{}]", cmd.vin(), order.getOrderNum());
        return order;
    }

    /**
     * 消费 VMD 车辆生产事实
     * <p>
     * 同一事件 ID 重复投递不重复建单；VMD 补发（新事件 ID、生产身份一致）复用既有单；
     * VIN 生产身份冲突为不可重试的确定性冲突。
     *
     * @param cmd 生产事实命令
     * @return 消费结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResultDto consumeVehicleProduced(ConsumeVehicleProducedCmd cmd) {
        Optional<InboxRecord> recorded = inboxRepository.find(InboxRecords.VMD_PRODUCE_CONSUMER,
                cmd.topic(), cmd.eventId());
        if (recorded.isPresent()) {
            return ConsumeResultDto.duplicate("事件[" + cmd.eventId() + "]已处理，返回已记录结果");
        }

        Optional<PreInboundOrder> existing = preInboundOrderRepository.findByVin(cmd.vin());
        if (existing.isPresent()) {
            PreInboundOrder order = existing.get();
            if (!sameProductionIdentity(order, cmd)) {
                inboxRepository.insert(InboxRecords.deadLettered(InboxRecords.VMD_PRODUCE_CONSUMER, cmd.topic(),
                        cmd.eventId(), "VehicleProduceEvent", cmd.payloadDigest(), cmd.vin(),
                        WmsErrorCode.VIN_NOT_AVAILABLE.getCode(),
                        "VIN[" + cmd.vin() + "]已存在不同生产身份的预入库单，拒绝覆盖"));
                return ConsumeResultDto.deadLetter("VIN 生产身份冲突，进入死信");
            }
            inboxRepository.insert(InboxRecords.processed(InboxRecords.VMD_PRODUCE_CONSUMER, cmd.topic(),
                    cmd.eventId(), "VehicleProduceEvent", cmd.payloadDigest(), cmd.vin(), null));
            logger.info("VMD 补发事件[{}]复用车辆[{}]预入库单[{}]，不新建", cmd.eventId(), cmd.vin(), order.getOrderNum());
            return ConsumeResultDto.processed("REUSED");
        }

        String warehouseCode = warehouseResolver.resolve(cmd.warehouseCode());
        PreInboundOrder order = PreInboundOrder.builder()
                .orderNum(OrderNumGenerator.generate(OrderNumGenerator.PRE_INBOUND_PREFIX))
                .vin(cmd.vin())
                .buildConfigCode(cmd.configurationCode())
                .warehouseCode(warehouseCode)
                .state(PreInboundState.PENDING_ARRIVAL)
                .sourceSystem(SOURCE_SYSTEM_VMD)
                .sourceEventId(cmd.eventId())
                .sourceBatchNum(cmd.batchNum())
                .rowVersion(1)
                .build();
        preInboundOrderRepository.insert(order);
        inboxRepository.insert(InboxRecords.processed(InboxRecords.VMD_PRODUCE_CONSUMER, cmd.topic(), cmd.eventId(),
                "VehicleProduceEvent", cmd.payloadDigest(), cmd.vin(), null));
        logger.info("消费 VMD 生产事实[{}]创建车辆[{}]预入库单[{}]", cmd.eventId(), cmd.vin(), order.getOrderNum());
        return ConsumeResultDto.processed("CREATED");
    }

    /**
     * 查询已记录的消息处理结果（用于并发重复事件的幂等确认）
     *
     * @param topic   来源 Topic
     * @param eventId 事件 ID
     * @return 已记录结果
     */
    public Optional<InboxRecord> findRecordedProduceResult(String topic, String eventId) {
        return inboxRepository.find(InboxRecords.VMD_PRODUCE_CONSUMER, topic, eventId);
    }

    private boolean sameProductionIdentity(PreInboundOrder order, ConsumeVehicleProducedCmd cmd) {
        boolean sameConfig = order.getBuildConfigCode() == null
                ? cmd.configurationCode() == null
                : order.getBuildConfigCode().equals(cmd.configurationCode());
        boolean sameWarehouse = cmd.warehouseCode() == null || cmd.warehouseCode().isBlank()
                || cmd.warehouseCode().equals(order.getWarehouseCode());
        boolean notCancelled = order.getState() != PreInboundState.CANCELLED;
        return sameConfig && sameWarehouse && notCancelled;
    }

}
