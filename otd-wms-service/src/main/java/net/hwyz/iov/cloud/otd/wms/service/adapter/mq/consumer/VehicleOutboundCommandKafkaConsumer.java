package net.hwyz.iov.cloud.otd.wms.service.adapter.mq.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.InboxRecords;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleOutboundCancellationCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleOutboundRequestedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.service.OutboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.ConsumeOutcome;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.common.util.DigestUtil;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.config.WmsKafkaProperties;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.NonRetryableConsumeException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 通用出库命令消费者
 * <p>
 * 消费 {@code wms.vehicle-outbound.requested}：按信封 eventType 分发请求／取消命令；
 * 合法但不可预留的命令产生业务拒绝事实，结构非法或幂等冲突进入消费者专属死信。
 *
 * @author hwyz_leo
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "wms.kafka.consumer.auto-startup", havingValue = "true", matchIfMissing = true)
public class VehicleOutboundCommandKafkaConsumer {

    private static final String EVENT_REQUESTED = "VehicleOutboundRequested";
    private static final String EVENT_CANCELLATION = "VehicleOutboundCancellationRequested";
    private static final String COMMAND_OPERATOR = "KAFKA_COMMAND";

    private final OutboundOrderAppService outboundOrderAppService;
    private final InboxRepository inboxRepository;
    private final WmsKafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    /**
     * 消费通用出库命令
     *
     * @param record Kafka 记录
     */
    @KafkaListener(
            id = "wmsVehicleOutboundCommandConsumer",
            topics = "${wms.kafka.topics.outbound-requested:wms.vehicle-outbound.requested}",
            groupId = "${wms.kafka.consumer.outbound-command-group:otd-wms-vehicle-outbound-command}",
            autoStartup = "${wms.kafka.consumer.auto-startup:true}",
            containerFactory = "wmsKafkaListenerContainerFactory"
    )
    public void onOutboundCommand(ConsumerRecord<String, String> record) {
        String topic = kafkaProperties.getTopics().getOutboundRequested();
        String payloadDigest = DigestUtil.sha256Hex(record.value());
        JsonNode root;
        try {
            root = objectMapper.readTree(record.value());
        } catch (Exception e) {
            deadLetter(topic, record, payloadDigest, "消息不是合法 JSON");
            throw new NonRetryableConsumeException("消息不是合法 JSON: " + e.getMessage(), e);
        }
        String eventId = text(root, "eventId");
        String eventType = text(root, "eventType");
        Integer schemaVersion = root.hasNonNull("schemaVersion") ? root.get("schemaVersion").asInt() : null;
        if (eventId == null || eventId.isBlank() || eventType == null || schemaVersion == null
                || schemaVersion != 1) {
            deadLetter(topic, record, payloadDigest, "事件信封或 schemaVersion 非法");
            throw new NonRetryableConsumeException("事件信封或 schemaVersion 非法");
        }
        JsonNode payload = root.path("payload");

        ConsumeResultDto result;
        try {
            result = switch (eventType) {
                case EVENT_REQUESTED -> outboundOrderAppService.consumeRequested(new VehicleOutboundRequestedCmd(
                        eventId, topic, payloadDigest, text(payload, "requestId"), text(payload, "sourceSystem"),
                        text(payload, "sourceOrderNum"), text(payload, "warehouseCode"), vins(payload),
                        COMMAND_OPERATOR));
                case EVENT_CANCELLATION -> outboundOrderAppService.consumeCancellation(
                        new VehicleOutboundCancellationCmd(eventId, topic, payloadDigest,
                                text(payload, "requestId"), text(payload, "sourceSystem"),
                                text(payload, "sourceOrderNum"), text(payload, "reason"), COMMAND_OPERATOR));
                default -> {
                    deadLetter(topic, record, payloadDigest, "未知事件类型: " + eventType);
                    throw new NonRetryableConsumeException("未知事件类型: " + eventType);
                }
            };
        } catch (DuplicateKeyException e) {
            Optional<InboxRecord> recorded = outboundOrderAppService.findRecordedCommandResult(topic, eventId);
            if (recorded.isPresent()) {
                logger.info("事件[{}]并发重复投递，按已记录结果确认", eventId);
                return;
            }
            throw e;
        }
        if (result.outcome() == ConsumeOutcome.DEAD_LETTER) {
            throw new NonRetryableConsumeException(result.detail());
        }
        logger.info("出库命令[{}]处理结果: {}, detail={}", eventId, result.outcome(), result.detail());
    }

    private List<String> vins(JsonNode payload) {
        JsonNode vins = payload.get("vins");
        if (vins == null || !vins.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>(vins.size());
        vins.forEach(node -> values.add(node.asText()));
        return values;
    }

    private void deadLetter(String topic, ConsumerRecord<String, String> record, String payloadDigest,
                            String message) {
        String eventId = "malformed-" + payloadDigest;
        try {
            inboxRepository.insert(InboxRecords.deadLettered(InboxRecords.OUTBOUND_COMMAND_CONSUMER, topic,
                    eventId, "UNKNOWN", payloadDigest, record.key(),
                    WmsErrorCode.MESSAGE_INVALID.getCode(), message));
        } catch (DuplicateKeyException e) {
            logger.info("非法命令[{}]已记录死信", eventId);
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
