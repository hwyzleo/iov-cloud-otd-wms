package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.config.WmsKafkaProperties;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.WmsKafkaLogicalTopic;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.WmsKafkaTopicDefinitionProvider;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.WmsKafkaTopicRoutes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Kafka Topic 注册表与死信路由测试
 *
 * @author hwyz_leo
 */
class WmsKafkaTopicRoutesTest {

    private WmsKafkaTopicRoutes routes() {
        return new WmsKafkaTopicRoutes(new WmsKafkaProperties());
    }

    @Test
    void shouldResolveRegisteredEventTypesOnly() {
        WmsKafkaTopicRoutes routes = routes();
        assertThat(routes.resolve("VehicleOutboundChangedEvent")).isEqualTo("wms.vehicle-outbound.changed");
        assertThat(routes.resolve("VehicleOutboundCommandRejectedEvent"))
                .isEqualTo("wms.vehicle-outbound.changed");
        assertThatThrownBy(() -> routes.resolve("UnknownEvent")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldMapConsumerSpecificDeadLetterTopics() {
        WmsKafkaTopicRoutes routes = routes();
        assertThat(routes.deadLetterTopic("vmd.vehicle-produce")).isEqualTo("vmd.vehicle-produce.wms.dlq");
        assertThat(routes.deadLetterTopic("wms.vehicle-outbound.requested"))
                .isEqualTo("wms.vehicle-outbound.requested.wms.dlq");
        assertThatThrownBy(() -> routes.deadLetterTopic("unknown.topic"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldProvisionOnlyWmsOwnedTopics() {
        WmsKafkaProperties properties = new WmsKafkaProperties();
        WmsKafkaTopicDefinitionProvider provider = new WmsKafkaTopicDefinitionProvider(routes(), properties);
        assertThat(provider.topicDefinitions())
                .extracting(def -> def.name())
                .containsExactlyInAnyOrder(
                        "wms.vehicle-outbound.requested",
                        "wms.vehicle-outbound.changed",
                        "vmd.vehicle-produce.wms.dlq",
                        "wms.vehicle-outbound.requested.wms.dlq")
                .doesNotContain("vmd.vehicle-produce");
        assertThat(WmsKafkaLogicalTopic.VEHICLE_PRODUCE.producerOwned()).isFalse();
    }
}
