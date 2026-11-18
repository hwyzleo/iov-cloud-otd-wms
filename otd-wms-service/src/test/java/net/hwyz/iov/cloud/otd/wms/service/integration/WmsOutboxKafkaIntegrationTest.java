package net.hwyz.iov.cloud.otd.wms.service.integration;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmArrivalCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CreatePreInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleOutboundRequestedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.OutboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.PreInboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.OutboxRelay;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.StorageAreaMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.StorageLocationMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.WarehouseMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageAreaPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageLocationPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WarehousePo;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Outbox 中继与 Kafka 发布集成测试（真实 Kafka）
 *
 * @author hwyz_leo
 */
class WmsOutboxKafkaIntegrationTest extends AbstractWmsIntegrationTest {

    private static final String TOPIC = "wms.vehicle-outbound.changed";

    @Autowired
    private PreInboundOrderAppService preInboundOrderAppService;
    @Autowired
    private InboundOrderAppService inboundOrderAppService;
    @Autowired
    private OutboundOrderAppService outboundOrderAppService;
    @Autowired
    private OutboxRelay outboxRelay;
    @Autowired
    private OutboundOrderRepository outboundOrderRepository;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private StorageAreaMapper storageAreaMapper;
    @Autowired
    private StorageLocationMapper storageLocationMapper;

    @Test
    void shouldRelayCommittedOutboxRecordToKafka() {
        prepareMasterData();
        String vin = prepareAvailableVin("VIN-KAFKA-1");

        outboundOrderAppService.consumeRequested(new VehicleOutboundRequestedCmd("EVT-" + UUID.randomUUID(),
                "wms.vehicle-outbound.requested", "digest", "REQ-K1", "TMS", "SRC-K1", null, List.of(vin), "0"));

        String orderNum = outboundOrderRepository.findBySourceOrder("TMS", "SRC-K1").orElseThrow().getOrderNum();
        String eventId = jdbcTemplate.queryForObject(
                "SELECT event_id FROM tb_wms_outbox WHERE aggregate_id = ?", String.class, orderNum);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT publish_state FROM tb_wms_outbox WHERE event_id = ?", String.class, eventId))
                .isEqualTo("PENDING");

        outboxRelay.relayMessages();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT publish_state FROM tb_wms_outbox WHERE event_id = ?", String.class, eventId))
                .isEqualTo("PUBLISHED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT published_at FROM tb_wms_outbox WHERE event_id = ?", Object.class, eventId)).isNotNull();

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                System.getenv().getOrDefault("WMS_IT_KAFKA", "127.0.0.1:9094"));
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "wms-it-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        boolean found = false;
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(List.of(TOPIC));
            long deadline = System.currentTimeMillis() + 30_000;
            while (!found && System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
                for (ConsumerRecord<String, String> record : records) {
                    if (record.value() != null && record.value().contains(eventId)) {
                        found = true;
                        assertThat(record.key()).isEqualTo(orderNum);
                        assertThat(record.value()).contains("VehicleOutboundChangedEvent")
                                .contains("\"schemaVersion\":2")
                                .doesNotContain("occurredAtEpochMillis")
                                .containsPattern("\"occurredAt\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{2}:\\d{2}\"");
                    }
                }
            }
        }
        assertThat(found).as("Kafka 中应能消费到 Outbox 中继发布的事件").isTrue();
    }

    private void prepareMasterData() {
        if (warehouseMapper.selectPoByCode("WH-DEFAULT") == null) {
            warehouseMapper.insertPo(WarehousePo.builder()
                    .code("WH-DEFAULT").name("默认仓库").warehouseLevel("PDC").enable(true).sort(1).build());
            storageAreaMapper.insertPo(StorageAreaPo.builder()
                    .code("A1").name("A1 区").warehouseCode("WH-DEFAULT").enable(true).sort(1).build());
        }
        if (storageLocationMapper.selectPoByWarehouseAreaAndCode("WH-DEFAULT", "A1", "L1") == null) {
            storageLocationMapper.insertPo(StorageLocationPo.builder()
                    .code("L1").name("L1").warehouseCode("WH-DEFAULT").storageAreaCode("A1")
                    .enable(true).sort(1).build());
        }
    }

    private String prepareAvailableVin(String vin) {
        PreInboundOrder order = preInboundOrderAppService.createPreInbound(new CreatePreInboundCmd(vin,
                "CFG-" + vin, null, "PDC", null, null, "9"));
        inboundOrderAppService.confirmArrival(new ConfirmArrivalCmd(order.getOrderNum(), "ARR-" + vin, null, "9"));
        inboundOrderAppService.confirmInbound(new ConfirmInboundCmd(order.getOrderNum(), "IN-" + vin, "A1",
                "L1", null, "9"));
        return vin;
    }
}
