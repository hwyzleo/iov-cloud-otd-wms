package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.api.contract.event.VehicleOutboundChangedEvent;
import net.hwyz.iov.cloud.otd.wms.api.contract.event.VehicleOutboundCommandRejectedEvent;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ItemReasonDto;
import net.hwyz.iov.cloud.otd.wms.service.application.port.OutboundFactPublisher;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboxPublishState;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.WmsKafkaTopicRoutes;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 出库事实发布实现
 * <p>
 * 事件载荷与 Outbox 记录在同一本地事务内写入；中继发布不回滚业务事实。
 *
 * @author hwyz_leo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundFactPublisherImpl implements OutboundFactPublisher {

    /**
     * 事件类型：出库单事实
     */
    public static final String EVENT_TYPE_CHANGED = "VehicleOutboundChangedEvent";
    /**
     * 事件类型：出库命令拒绝事实
     */
    public static final String EVENT_TYPE_REJECTED = "VehicleOutboundCommandRejectedEvent";
    /**
     * 生产者标识
     */
    private static final String PRODUCER = "otd-wms";
    /**
     * 聚合类型
     */
    private static final String AGGREGATE_TYPE = "OUTBOUND_ORDER";
    /**
     * 载荷版本
     */
    private static final int SCHEMA_VERSION = 1;

    private final OutboxRepository outboxRepository;
    private final WmsKafkaTopicRoutes topicRoutes;
    private final ObjectMapper objectMapper;

    @Override
    public void publishChanged(OutboundOrder order, String requestId) {
        VehicleOutboundChangedEvent event = VehicleOutboundChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EVENT_TYPE_CHANGED)
                .schemaVersion(SCHEMA_VERSION)
                .occurredAtEpochMillis(System.currentTimeMillis())
                .producer(PRODUCER)
                .aggregateVersion(order.getAggregateVersion())
                .payload(VehicleOutboundChangedEvent.Payload.builder()
                        .outboundOrderNum(order.getOrderNum())
                        .orderState(order.getState() == null ? null : order.getState().name())
                        .sourceSystem(order.getSourceSystem())
                        .sourceOrderNum(order.getSourceOrderNum())
                        .warehouseCode(order.getWarehouseCode())
                        .requestId(requestId)
                        .items(order.getDetails().stream().map(this::toItem).toList())
                        .build())
                .build();
        insertOutbox(event.getEventId(), EVENT_TYPE_CHANGED, order.getOrderNum(),
                order.getAggregateVersion() == null ? 0L : order.getAggregateVersion(),
                order.getOrderNum(), event);
    }

    @Override
    public void publishRejected(String requestId, String sourceSystem, String sourceOrderNum,
                                String outboundOrderNum, String reasonCode, List<ItemReasonDto> itemReasons,
                                long aggregateVersion) {
        VehicleOutboundCommandRejectedEvent event = VehicleOutboundCommandRejectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(EVENT_TYPE_REJECTED)
                .schemaVersion(SCHEMA_VERSION)
                .occurredAtEpochMillis(System.currentTimeMillis())
                .producer(PRODUCER)
                .aggregateVersion(aggregateVersion)
                .payload(VehicleOutboundCommandRejectedEvent.Payload.builder()
                        .requestId(requestId)
                        .sourceSystem(sourceSystem)
                        .sourceOrderNum(sourceOrderNum)
                        .outboundOrderNum(outboundOrderNum)
                        .reasonCode(reasonCode)
                        .itemReasons(itemReasons == null ? List.of() : itemReasons.stream()
                                .map(reason -> VehicleOutboundCommandRejectedEvent.ItemReason.builder()
                                        .vin(reason.vin())
                                        .reasonCode(reason.reasonCode())
                                        .build())
                                .toList())
                        .build())
                .build();
        String messageKey = outboundOrderNum == null || outboundOrderNum.isBlank() ? sourceOrderNum : outboundOrderNum;
        insertOutbox(event.getEventId(), EVENT_TYPE_REJECTED,
                outboundOrderNum == null ? sourceOrderNum : outboundOrderNum,
                aggregateVersion, messageKey, event);
    }

    private VehicleOutboundChangedEvent.Item toItem(OutboundOrderDetail detail) {
        return VehicleOutboundChangedEvent.Item.builder()
                .vin(detail.getVin())
                .state(detail.getState() == null ? null : detail.getState().name())
                .reserveTimeEpochMillis(epochMillis(detail.getReserveTime()))
                .loadTimeEpochMillis(epochMillis(detail.getLoadTime()))
                .outboundTimeEpochMillis(epochMillis(detail.getOutboundTime()))
                .releaseTimeEpochMillis(epochMillis(detail.getReleaseTime()))
                .build();
    }

    private Long epochMillis(java.util.Date date) {
        return date == null ? null : date.getTime();
    }

    private void insertOutbox(String eventId, String eventType, String aggregateId, long aggregateVersion,
                              String messageKey, Object event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("出库事件序列化失败: " + eventType, e);
        }
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
