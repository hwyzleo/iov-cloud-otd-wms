package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.util.ExplicitTimeUtil;
import net.hwyz.iov.cloud.otd.wms.api.contract.event.VehicleInboundChangedEvent;
import net.hwyz.iov.cloud.otd.wms.api.contract.event.VehicleInboundCommandRejectedEvent;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ItemReasonDto;
import net.hwyz.iov.cloud.otd.wms.service.application.port.InboundFactPublisher;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboxPublishState;
import net.hwyz.iov.cloud.otd.wms.service.common.util.WmsEventTime;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.InboundTask;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.WmsKafkaTopicRoutes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * 入库事实发布实现
 * <p>
 * 事件载荷与 Outbox 记录在同一本地事务内写入；中继发布不回滚业务事实。
 *
 * @author hwyz_leo
 */
@Slf4j
@Component
public class InboundFactPublisherImpl implements InboundFactPublisher {

    /**
     * 事件类型：待入库任务事实
     */
    public static final String EVENT_TYPE_CHANGED = "VehicleInboundChangedEvent";
    /**
     * 事件类型：到货命令拒绝事实
     */
    public static final String EVENT_TYPE_REJECTED = "VehicleInboundCommandRejectedEvent";
    /**
     * 生产者标识
     */
    private static final String PRODUCER = "otd-wms";
    /**
     * 聚合类型
     */
    private static final String AGGREGATE_TYPE = "INBOUND_TASK";
    /**
     * 载荷版本（与 CR-002 口径一致）
     */
    private static final int SCHEMA_VERSION = 2;

    private final OutboxRepository outboxRepository;
    private final WmsKafkaTopicRoutes topicRoutes;
    private final ZoneId eventZone;

    public InboundFactPublisherImpl(OutboxRepository outboxRepository,
                                    WmsKafkaTopicRoutes topicRoutes,
                                    @Value("${wms.event.time-zone:Asia/Shanghai}") String eventTimeZone) {
        this.outboxRepository = outboxRepository;
        this.topicRoutes = topicRoutes;
        this.eventZone = ZoneId.of(eventTimeZone);
    }

    @Override
    public void publishChanged(InboundTask task, String requestId) {
        VehicleInboundChangedEvent event = VehicleInboundChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EVENT_TYPE_CHANGED)
                .schemaVersion(SCHEMA_VERSION)
                .occurredAt(WmsEventTime.format(ExplicitTimeUtil.nowInstant(), eventZone))
                .producer(PRODUCER)
                .aggregateVersion(task.getAggregateVersion())
                .payload(VehicleInboundChangedEvent.Payload.builder()
                        .inboundTaskNum(task.getOrderNum())
                        .vin(task.getVin())
                        .taskState(task.getState() == null ? null : task.getState().name())
                        .lifecycleSeq(task.getLifecycleSeq())
                        .sourceSystem(task.getSourceSystem())
                        .sourceOrderNum(task.getSourceOrderNum())
                        .warehouseCode(task.getWarehouseCode())
                        .requestId(requestId)
                        .arrivalTime(WmsEventTime.format(task.getArrivalTime(), eventZone))
                        .inboundTime(WmsEventTime.format(task.getInboundTime(), eventZone))
                        .cancelTime(WmsEventTime.format(task.getCancelTime(), eventZone))
                        .build())
                .build();
        insertOutbox(event.getEventId(), EVENT_TYPE_CHANGED, task.getOrderNum(),
                task.getAggregateVersion() == null ? 0L : task.getAggregateVersion(), task.getOrderNum(), event);
    }

    @Override
    public void publishRejected(String requestId, String sourceSystem, String sourceOrderNum,
                                String inboundTaskNum, String reasonCode, List<ItemReasonDto> itemReasons,
                                long aggregateVersion) {
        VehicleInboundCommandRejectedEvent event = VehicleInboundCommandRejectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EVENT_TYPE_REJECTED)
                .schemaVersion(SCHEMA_VERSION)
                .occurredAt(WmsEventTime.format(ExplicitTimeUtil.nowInstant(), eventZone))
                .producer(PRODUCER)
                .aggregateVersion(aggregateVersion)
                .payload(VehicleInboundCommandRejectedEvent.Payload.builder()
                        .requestId(requestId)
                        .sourceSystem(sourceSystem)
                        .sourceOrderNum(sourceOrderNum)
                        .inboundTaskNum(inboundTaskNum)
                        .reasonCode(reasonCode)
                        .itemReasons(itemReasons == null ? List.of() : itemReasons.stream()
                                .map(reason -> VehicleInboundCommandRejectedEvent.ItemReason.builder()
                                        .vin(reason.vin())
                                        .reasonCode(reason.reasonCode())
                                        .build())
                                .toList())
                        .build())
                .build();
        String messageKey = inboundTaskNum == null || inboundTaskNum.isBlank() ? sourceOrderNum : inboundTaskNum;
        insertOutbox(event.getEventId(), EVENT_TYPE_REJECTED,
                inboundTaskNum == null ? sourceOrderNum : inboundTaskNum,
                aggregateVersion, messageKey, event);
    }

    private void insertOutbox(String eventId, String eventType, String aggregateId, long aggregateVersion,
                              String messageKey, Object event) {
        String payload = WmsEventPayloadSerializer.write(event);
        outboxRepository.insert(OutboxRecord.builder()
                .eventId(eventId)
                .eventType(eventType)
                .aggregateType(AGGREGATE_TYPE)
                .aggregateId(aggregateId)
                .aggregateVersion(aggregateVersion)
                .topic(topicRoutes.resolve(eventType))
                .messageKey(messageKey)
                .payload(payload)
                .publishState(OutboxPublishState.PENDING)
                .retryCount(0)
                .build());
    }
}
