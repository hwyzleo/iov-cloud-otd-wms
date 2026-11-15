package net.hwyz.iov.cloud.otd.wms.service.adapter.mq.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.InboxRecords;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConsumeVehicleProducedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.service.PreInboundOrderAppService;
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

import java.util.Optional;

/**
 * VMD 车辆生产事件消费者
 * <p>
 * 消费 {@code vmd.vehicle-produce}：校验信封与事件类型，按 Inbox 幂等写入预入库单；
 * 结构非法或 VIN 生产身份冲突不重试，直接路由到消费者专属死信。
 *
 * @author hwyz_leo
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "wms.kafka.consumer.auto-startup", havingValue = "true", matchIfMissing = true)
public class VehicleProduceKafkaConsumer {

    /**
     * VMD 生产事件类型
     */
    private static final String EVENT_TYPE = "VehicleProduceEvent";

    private final PreInboundOrderAppService preInboundOrderAppService;
    private final InboxRepository inboxRepository;
    private final WmsKafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    /**
     * 消费 VMD 车辆生产事实
     *
     * @param record Kafka 记录
     */
    @KafkaListener(
            id = "wmsVehicleProduceConsumer",
            topics = "${wms.kafka.topics.vehicle-produce:vmd.vehicle-produce}",
            groupId = "${wms.kafka.consumer.vehicle-produce-group:otd-wms-vehicle-produce}",
            autoStartup = "${wms.kafka.consumer.auto-startup:true}",
            containerFactory = "wmsKafkaListenerContainerFactory"
    )
    public void onVehicleProduce(ConsumerRecord<String, String> record) {
        String topic = kafkaProperties.getTopics().getVehicleProduce();
        String payloadDigest = DigestUtil.sha256Hex(record.value());
        VehicleProducedMessage message = parse(record, topic, payloadDigest);

        ConsumeVehicleProducedCmd cmd = new ConsumeVehicleProducedCmd(message.eventId(), topic,
                message.batchNum(), message.vin(), message.configurationCode(), message.warehouseCode(),
                payloadDigest);
        ConsumeResultDto result;
        try {
            result = preInboundOrderAppService.consumeVehicleProduced(cmd);
        } catch (DuplicateKeyException e) {
            Optional<InboxRecord> recorded = preInboundOrderAppService.findRecordedProduceResult(topic,
                    message.eventId());
            if (recorded.isPresent()) {
                log.info("事件[{}]并发重复投递，按已记录结果确认", message.eventId());
                return;
            }
            throw e;
        }
        if (result.outcome() == net.hwyz.iov.cloud.otd.wms.service.common.enums.ConsumeOutcome.DEAD_LETTER) {
            throw new NonRetryableConsumeException(result.detail());
        }
        log.info("VMD 生产事件[{}]处理结果: {}, detail={}", message.eventId(), result.outcome(), result.detail());
    }

    private VehicleProducedMessage parse(ConsumerRecord<String, String> record, String topic, String payloadDigest) {
        JsonNode root;
        try {
            root = objectMapper.readTree(record.value());
        } catch (Exception e) {
            deadLetter(topic, record, payloadDigest, "消息不是合法 JSON");
            throw new NonRetryableConsumeException("消息不是合法 JSON: " + e.getMessage(), e);
        }
        String eventType = text(root, "eventType");
        String eventId = text(root, "eventId");
        if (eventId == null || eventId.isBlank() || eventType == null || !EVENT_TYPE.equals(eventType)) {
            deadLetter(topic, record, payloadDigest, "事件信封或事件类型非法");
            throw new NonRetryableConsumeException("事件信封或事件类型非法");
        }
        JsonNode payload = root.path("payload");
        String vin = text(payload, "vin");
        String configurationCode = text(payload, "configurationCode");
        if (vin == null || vin.isBlank() || configurationCode == null || configurationCode.isBlank()) {
            deadLetter(topic, record, payloadDigest, "缺少必要业务字段 vin/configurationCode");
            throw new NonRetryableConsumeException("缺少必要业务字段 vin/configurationCode");
        }
        String batchNum = text(root, "batchNum");
        String warehouseCode = text(payload, "warehouseCode");
        return new VehicleProducedMessage(eventId, vin, configurationCode, warehouseCode, batchNum);
    }

    private void deadLetter(String topic, ConsumerRecord<String, String> record, String payloadDigest,
                            String message) {
        String eventId = "malformed-" + payloadDigest;
        try {
            inboxRepository.insert(InboxRecords.deadLettered(InboxRecords.VMD_PRODUCE_CONSUMER, topic, eventId,
                    EVENT_TYPE, payloadDigest, record.key(),
                    net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode.MESSAGE_INVALID.getCode(),
                    message));
        } catch (DuplicateKeyException e) {
            log.info("非法消息[{}]已记录死信", eventId);
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private record VehicleProducedMessage(String eventId, String vin, String configurationCode,
                                           String warehouseCode, String batchNum) {
    }
}
