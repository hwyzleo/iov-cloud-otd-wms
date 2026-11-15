package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.config.WmsKafkaProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * WMS Kafka Topic 路由注册表
 * <p>
 * Outbox 记录写入与 Relay 发布统一引用本注册表：按事件类型解析注册 Topic，
 * 未知事件类型 fail-fast，禁止通过字符串拼接推导 Topic。
 *
 * @author hwyz_leo
 */
@Component
public class WmsKafkaTopicRoutes {

    /**
     * 事件类型 → 逻辑 Topic 映射（注册表）
     */
    private static final Map<String, WmsKafkaLogicalTopic> EVENT_TYPE_ROUTES = Map.of(
            "VehicleOutboundChangedEvent", WmsKafkaLogicalTopic.OUTBOUND_CHANGED,
            "VehicleOutboundCommandRejectedEvent", WmsKafkaLogicalTopic.OUTBOUND_CHANGED
    );

    private final WmsKafkaProperties properties;

    public WmsKafkaTopicRoutes(WmsKafkaProperties properties) {
        this.properties = properties;
    }

    /**
     * 按逻辑名读取实际 Topic 名称
     *
     * @param logical 逻辑名
     * @return Topic 名称
     */
    public String topicName(WmsKafkaLogicalTopic logical) {
        return switch (logical) {
            case VEHICLE_PRODUCE -> properties.getTopics().getVehicleProduce();
            case OUTBOUND_REQUESTED -> properties.getTopics().getOutboundRequested();
            case OUTBOUND_CHANGED -> properties.getTopics().getOutboundChanged();
            case VEHICLE_PRODUCE_DLQ -> properties.getTopics().getVehicleProduceDlq();
            case OUTBOUND_REQUESTED_DLQ -> properties.getTopics().getOutboundRequestedDlq();
        };
    }

    /**
     * 按事件类型解析注册 Topic（fail-fast）
     *
     * @param eventType 事件类型
     * @return Topic 名称
     */
    public String resolve(String eventType) {
        WmsKafkaLogicalTopic logical = EVENT_TYPE_ROUTES.get(eventType);
        if (logical == null) {
            throw new IllegalArgumentException("未知逻辑事件类型，拒绝路由: " + eventType);
        }
        return topicName(logical);
    }

    /**
     * 安全解析（不抛异常）
     *
     * @param eventType 事件类型
     * @return Topic 名称
     */
    public Optional<String> resolveIfRegistered(String eventType) {
        WmsKafkaLogicalTopic logical = EVENT_TYPE_ROUTES.get(eventType);
        return logical == null ? Optional.empty() : Optional.of(topicName(logical));
    }

    /**
     * 解析消费者专属死信 Topic
     *
     * @param sourceTopic 来源 Topic
     * @return 死信 Topic 名称
     */
    public String deadLetterTopic(String sourceTopic) {
        if (properties.getTopics().getVehicleProduce().equals(sourceTopic)) {
            return topicName(WmsKafkaLogicalTopic.VEHICLE_PRODUCE_DLQ);
        }
        if (properties.getTopics().getOutboundRequested().equals(sourceTopic)) {
            return topicName(WmsKafkaLogicalTopic.OUTBOUND_REQUESTED_DLQ);
        }
        throw new IllegalArgumentException("来源 Topic 未注册死信目的地: " + sourceTopic);
    }
}
