package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka;

import net.hwyz.iov.cloud.framework.kafka.topic.KafkaTopicDefinition;
import net.hwyz.iov.cloud.framework.kafka.topic.KafkaTopicDefinitionProvider;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.config.WmsKafkaProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * WMS Kafka Topic 定义提供者
 * <p>
 * WMS 仅声明并初始化自身拥有的 Topic；VMD Topic 只消费不创建。
 *
 * @author hwyz_leo
 */
@Component
public class WmsKafkaTopicDefinitionProvider implements KafkaTopicDefinitionProvider {

    private final WmsKafkaTopicRoutes topicRoutes;
    private final WmsKafkaProperties properties;

    public WmsKafkaTopicDefinitionProvider(WmsKafkaTopicRoutes topicRoutes, WmsKafkaProperties properties) {
        this.topicRoutes = topicRoutes;
        this.properties = properties;
    }

    @Override
    public Collection<KafkaTopicDefinition> topicDefinitions() {
        List<WmsKafkaLogicalTopic> owned = Arrays.stream(WmsKafkaLogicalTopic.values())
                .filter(WmsKafkaLogicalTopic::producerOwned)
                .toList();
        List<KafkaTopicDefinition> definitions = new ArrayList<>(owned.size());
        for (WmsKafkaLogicalTopic logical : owned) {
            definitions.add(new KafkaTopicDefinition(
                    topicRoutes.topicName(logical),
                    properties.getProvisioning().getPartitions(),
                    properties.getProvisioning().getReplicationFactor()));
        }
        return definitions;
    }
}
