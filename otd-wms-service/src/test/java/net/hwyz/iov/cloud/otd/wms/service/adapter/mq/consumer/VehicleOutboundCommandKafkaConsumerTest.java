package net.hwyz.iov.cloud.otd.wms.service.adapter.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleOutboundRequestedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.service.OutboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.config.WmsKafkaProperties;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.NonRetryableConsumeException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 出库命令消费者信封校验测试（OTD-WMS-DSN-CR-002）
 * <p>
 * schemaVersion=2 的命令正常分发；schemaVersion=1（改造前格式）与非法结构进死信，不再处理。
 *
 * @author hwyz_leo
 */
class VehicleOutboundCommandKafkaConsumerTest {

    private static final String TOPIC = "wms.vehicle-outbound.requested";

    private final OutboundOrderAppService appService = mock(OutboundOrderAppService.class);
    private final List<InboxRecord> deadLetters = new ArrayList<>();

    private final InboxRepository inboxRepository = new InboxRepository() {
        @Override
        public void insert(InboxRecord record) {
            deadLetters.add(record);
        }

        @Override
        public java.util.Optional<InboxRecord> find(String consumerName, String topic, String eventId) {
            return java.util.Optional.empty();
        }
    };

    private VehicleOutboundCommandKafkaConsumer consumer() {
        return new VehicleOutboundCommandKafkaConsumer(appService, inboxRepository,
                new WmsKafkaProperties(), new ObjectMapper());
    }

    private String envelope(int schemaVersion) {
        return """
                {"eventId":"EVT-1","eventType":"VehicleOutboundRequested","schemaVersion":%d,
                 "occurredAt":"2026-11-18T13:05:27.412+08:00","producer":"otd-tms",
                 "payload":{"requestId":"REQ-1","sourceSystem":"TMS","sourceOrderNum":"TO-1",
                            "warehouseCode":"WH-DEFAULT","vins":["VIN0000000000001"]}}
                """.formatted(schemaVersion);
    }

    @Test
    void shouldDispatchSchemaVersionTwoCommand() {
        when(appService.consumeRequested(any(VehicleOutboundRequestedCmd.class)))
                .thenReturn(ConsumeResultDto.processed("已预留"));

        consumer().onOutboundCommand(new ConsumerRecord<>(TOPIC, 0, 0, "TO-1", envelope(2)));

        assertThat(deadLetters).isEmpty();
    }

    @Test
    void shouldDeadLetterLegacySchemaVersionOneCommand() {
        assertThatThrownBy(() ->
                consumer().onOutboundCommand(new ConsumerRecord<>(TOPIC, 0, 0, "TO-1", envelope(1))))
                .isInstanceOf(NonRetryableConsumeException.class);
        assertThat(deadLetters).hasSize(1);
        assertThat(deadLetters.get(0).getErrorMessage()).contains("schemaVersion=2");
        assertThat(deadLetters.get(0).getTopic()).isEqualTo(TOPIC);
    }

    @Test
    void shouldDeadLetterMalformedEnvelope() {
        assertThatThrownBy(() ->
                consumer().onOutboundCommand(new ConsumerRecord<>(TOPIC, 0, 0, "TO-1", "{\"eventId\":\"EVT-1\"}")))
                .isInstanceOf(NonRetryableConsumeException.class);
        assertThat(deadLetters).hasSize(1);
    }
}
