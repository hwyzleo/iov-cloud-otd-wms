package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ItemReasonDto;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundDetailState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundOrderState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboxPublishState;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.config.WmsKafkaProperties;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.WmsKafkaTopicRoutes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 出库事实发布器测试（OTD-WMS-DSN-CR-002）
 * <p>
 * 断言事件信封与载荷时间字段为 ISO-8601，schemaVersion=2，且不再出现 *EpochMillis 字段。
 *
 * @author hwyz_leo
 */
class OutboundFactPublisherImplTest {

    private static final String ISO_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{2}:\\d{2}";

    private final List<OutboxRecord> captured = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OutboxRepository outboxRepository = new OutboxRepository() {
        @Override
        public void insert(OutboxRecord record) {
            captured.add(record);
        }

        @Override
        public List<OutboxRecord> listPublishable(Date now, int limit) {
            return List.of();
        }

        @Override
        public int markPublished(Long id) {
            return 0;
        }

        @Override
        public int markRetryable(Long id, int retryCount, Date nextRetryTime, String error) {
            return 0;
        }

        @Override
        public int markDead(Long id, String error) {
            return 0;
        }
    };

    @BeforeEach
    void setUp() {
        captured.clear();
    }

    private OutboundFactPublisherImpl publisher(String zone) {
        return new OutboundFactPublisherImpl(outboxRepository,
                new WmsKafkaTopicRoutes(new WmsKafkaProperties()), zone);
    }

    private OutboundOrder order() {
        return OutboundOrder.builder()
                .orderNum("OB-0001")
                .requestId("REQ-0001")
                .sourceSystem("TMS")
                .sourceOrderNum("SRC-0001")
                .warehouseCode("WH-DEFAULT")
                .state(OutboundOrderState.PARTIALLY_OUTBOUND)
                .aggregateVersion(3L)
                .details(List.of(OutboundOrderDetail.builder()
                        .orderNum("OB-0001")
                        .vin("VIN0000000000001")
                        .state(OutboundDetailState.OUTBOUND)
                        .reserveTime(Date.from(Instant.parse("2026-11-18T05:05:27.412Z")))
                        .loadTime(Date.from(Instant.parse("2026-11-18T06:06:28.500Z")))
                        .outboundTime(Date.from(Instant.parse("2026-11-18T07:07:29.600Z")))
                        .build()))
                .build();
    }

    @Test
    void shouldPublishChangedFactWithIsoTimestamps() throws Exception {
        publisher("Asia/Shanghai").publishChanged(order(), "REQ-0001");

        assertThat(captured).hasSize(1);
        OutboxRecord record = captured.get(0);
        assertThat(record.getEventType()).isEqualTo("VehicleOutboundChangedEvent");
        assertThat(record.getTopic()).isEqualTo("wms.vehicle-outbound.changed");
        assertThat(record.getMessageKey()).isEqualTo("OB-0001");
        assertThat(record.getPublishState()).isEqualTo(OutboxPublishState.PENDING);

        JsonNode json = objectMapper.readTree(record.getPayload());
        assertThat(json.get("schemaVersion").asInt()).isEqualTo(2);
        assertThat(json.get("occurredAt").asText()).matches(ISO_PATTERN);
        assertThat(json.has("occurredAtEpochMillis")).isFalse();
        assertThat(json.get("aggregateVersion").asLong()).isEqualTo(3L);

        JsonNode item = json.path("payload").path("items").get(0);
        assertThat(item.get("reserveTime").asText()).isEqualTo("2026-11-18T13:05:27.412+08:00");
        assertThat(item.get("loadTime").asText()).isEqualTo("2026-11-18T14:06:28.500+08:00");
        assertThat(item.get("outboundTime").asText()).isEqualTo("2026-11-18T15:07:29.600+08:00");
        assertThat(item.get("releaseTime").isNull()).isTrue();
        assertThat(item.has("reserveTimeEpochMillis")).isFalse();
        assertThat(item.has("releaseTimeEpochMillis")).isFalse();
    }

    @Test
    void shouldPublishRejectedFactWithIsoOccurredAt() throws Exception {
        publisher("Asia/Shanghai").publishRejected("REQ-0002", "TMS", "SRC-0002", null,
                "302003", List.of(new ItemReasonDto("VIN0000000000002", "302003")), 0L);

        assertThat(captured).hasSize(1);
        JsonNode json = objectMapper.readTree(captured.get(0).getPayload());
        assertThat(json.get("schemaVersion").asInt()).isEqualTo(2);
        assertThat(json.get("occurredAt").asText()).matches(ISO_PATTERN);
        assertThat(json.has("occurredAtEpochMillis")).isFalse();
        assertThat(json.path("payload").path("reasonCode").asText()).isEqualTo("302003");
        assertThat(json.path("payload").path("itemReasons").get(0).path("vin").asText())
                .isEqualTo("VIN0000000000002");
    }

    @Test
    void shouldFollowConfiguredEventZone() throws Exception {
        publisher("UTC").publishChanged(order(), "REQ-0003");

        JsonNode json = objectMapper.readTree(captured.get(0).getPayload());
        assertThat(json.get("occurredAt").asText()).endsWith("Z");
        assertThat(json.path("payload").path("items").get(0).path("reserveTime").asText())
                .isEqualTo("2026-11-18T05:05:27.412Z");
    }
}
