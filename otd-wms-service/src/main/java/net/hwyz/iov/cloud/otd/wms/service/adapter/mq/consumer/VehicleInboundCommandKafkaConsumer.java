package net.hwyz.iov.cloud.otd.wms.service.adapter.mq.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleInboundCancellationCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleInboundRequestedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InboundTaskAppService;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.ConsumeOutcome;
import net.hwyz.iov.cloud.otd.wms.service.common.util.DigestUtil;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.config.WmsKafkaProperties;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.NonRetryableConsumeException;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.WmsKafkaTopicRoutes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用到货命令消费者
 * <p>
 * 消费 {@code wms.vehicle-inbound.requested}：按信封 eventType 分发到货请求／取消命令；
 * 合法但不可受理的命令产生业务拒绝事实，结构非法进入消费者专属死信。
 *
 * @author hwyz_leo
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "wms.kafka.consumer.auto-startup", havingValue = "true", matchIfMissing = true)
public class VehicleInboundCommandKafkaConsumer {

    private static final String EVENT_REQUESTED = "VehicleInboundRequested";
    private static final String EVENT_CANCELLATION = "VehicleInboundCancellationRequested";
    private static final String COMMAND_OPERATOR = "KAFKA_COMMAND";
    /**
     * 命令信封载荷版本（与 CR-002 口径一致）
     */
    private static final int COMMAND_SCHEMA_VERSION = 2;

    private final InboundTaskAppService inboundTaskAppService;
    private final WmsKafkaProperties kafkaProperties;
    private final WmsKafkaTopicRoutes topicRoutes;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 消费通用到货命令
     *
     * @param record Kafka 记录
     */
    @KafkaListener(
            id = "wmsVehicleInboundCommandConsumer",
            topics = "${wms.kafka.topics.inbound-requested:wms.vehicle-inbound.requested}",
            groupId = "${wms.kafka.consumer.inbound-command-group:otd-wms-vehicle-inbound-command}",
            autoStartup = "${wms.kafka.consumer.auto-startup:true}",
            containerFactory = "wmsKafkaListenerContainerFactory"
    )
    public void onInboundCommand(ConsumerRecord<String, String> record) {
        String topic = kafkaProperties.getTopics().getInboundRequested();
        String payloadDigest = DigestUtil.sha256Hex(record.value());
        JsonNode root;
        try {
            root = objectMapper.readTree(record.value());
        } catch (Exception e) {
            deadLetter(record, topic, payloadDigest, "消息不是合法 JSON");
            throw new NonRetryableConsumeException("消息不是合法 JSON: " + e.getMessage(), e);
        }
        String eventId = text(root, "eventId");
        String eventType = text(root, "eventType");
        Integer schemaVersion = root.hasNonNull("schemaVersion") ? root.get("schemaVersion").asInt() : null;
        if (eventId == null || eventId.isBlank() || eventType == null || schemaVersion == null
                || schemaVersion != COMMAND_SCHEMA_VERSION) {
            deadLetter(record, topic, payloadDigest, "事件信封或 schemaVersion 非法");
            throw new NonRetryableConsumeException("事件信封或 schemaVersion 非法（要求 schemaVersion="
                    + COMMAND_SCHEMA_VERSION + "）");
        }
        JsonNode payload = root.path("payload");
        ConsumeResultDto result;
        try {
            result = switch (eventType) {
                case EVENT_REQUESTED -> inboundTaskAppService.consumeRequested(new VehicleInboundRequestedCmd(
                        eventId, topic, payloadDigest, text(payload, "requestId"), text(payload, "sourceSystem"),
                        text(payload, "sourceOrderNum"), text(payload, "warehouseCode"), vins(payload),
                        text(payload, "estimatedArrivalTime"), COMMAND_OPERATOR));
                case EVENT_CANCELLATION -> inboundTaskAppService.consumeCancellation(
                        new VehicleInboundCancellationCmd(eventId, topic, payloadDigest,
                                text(payload, "requestId"), text(payload, "sourceSystem"),
                                text(payload, "sourceOrderNum"), text(payload, "reason"), COMMAND_OPERATOR));
                default -> {
                    deadLetter(record, topic, payloadDigest, "未知事件类型: " + eventType);
                    throw new NonRetryableConsumeException("未知事件类型: " + eventType);
                }
            };
        } catch (NonRetryableConsumeException e) {
            throw e;
        }
        if (result.outcome() == ConsumeOutcome.DEAD_LETTER) {
            throw new NonRetryableConsumeException(result.detail());
        }
        log.info("到货命令[{}]处理结果: {}, detail={}", eventId, result.outcome(), result.detail());
    }

    private List<String> vins(JsonNode payload) {
        JsonNode vins = payload.get("vins");
        if (vins == null || !vins.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>(vins.size());
        vins.forEach(node -> values.add(node.isNull() ? null : node.asText()));
        return values;
    }

    private void deadLetter(ConsumerRecord<String, String> record, String sourceTopic, String payloadDigest,
                            String reason) {
        try {
            kafkaTemplate.send(topicRoutes.deadLetterTopic(sourceTopic), record.key(), record.value());
        } catch (Exception e) {
            log.error("到货命令死信转发失败[{}]: {}", payloadDigest, e.getMessage(), e);
        }
        log.error("到货命令非法，转死信: key={}, reason={}", record.key(), reason);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
