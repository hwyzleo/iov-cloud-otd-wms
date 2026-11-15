package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.config.WmsKafkaProperties;

import java.util.Map;

/**
 * WMS Kafka 消费者配置
 * <p>
 * 有界重试 + 消费者专属死信：临时故障按退避重试，超过上限或确定性冲突路由到
 * 注册的死信 Topic；业务提交在事务内完成，中继发布不回滚已提交事实。
 *
 * @author hwyz_leo
 */
@Configuration
public class WmsKafkaConsumerConfig {

    /**
     * WMS 消费者容器工厂
     *
     * @param kafkaProperties  Spring Kafka 配置
     * @param kafkaTemplate    Kafka 生产模板
     * @param wmsKafkaProperties WMS Kafka 类型化配置
     * @param topicRoutes     Topic 路由注册表
     * @return 容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> wmsKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties,
            KafkaTemplate<String, String> kafkaTemplate,
            WmsKafkaProperties wmsKafkaProperties,
            WmsKafkaTopicRoutes topicRoutes) {
        Map<String, Object> consumerProps = kafkaProperties.buildConsumerProperties();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProps));

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new org.apache.kafka.common.TopicPartition(
                        topicRoutes.deadLetterTopic(record.topic()), record.partition()));

        WmsKafkaProperties.Retry retry = wmsKafkaProperties.getConsumer().getRetry();
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer,
                new FixedBackOff(retry.getBackoffMs(), retry.getMaxAttempts()));
        errorHandler.addNotRetryableExceptions(NonRetryableConsumeException.class);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
