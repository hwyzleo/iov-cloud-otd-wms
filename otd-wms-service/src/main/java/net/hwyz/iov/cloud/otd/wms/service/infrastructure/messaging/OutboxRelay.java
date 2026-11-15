package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.config.WmsOutboxProperties;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka.WmsKafkaTopicRoutes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Outbox 中继
 * <p>
 * 按有界批次扫描 PENDING／FAILED_RETRYABLE 记录，使用不可变载荷与注册 Topic／Key 发布，
 * 仅在 Broker 确认后标记 PUBLISHED；超过重试上限或无法路由置 DEAD。
 *
 * @author hwyz_leo
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "wms.outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {

    private static final int MAX_ERROR_LENGTH = 2000;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WmsKafkaTopicRoutes topicRoutes;
    private final WmsOutboxProperties outboxProperties;

    /**
     * 定时扫描并发布 Outbox 记录
     */
    @Scheduled(fixedDelayString = "${wms.outbox.relay.scan-interval-ms:10000}")
    public void relayMessages() {
        List<OutboxRecord> records = outboxRepository.listPublishable(new Date(), outboxProperties.getBatchSize());
        if (records.isEmpty()) {
            return;
        }
        log.info("Outbox 中继扫描到 {} 条待发布记录", records.size());
        for (OutboxRecord record : records) {
            relayOne(record);
        }
    }

    private void relayOne(OutboxRecord record) {
        Optional<String> expectedTopic = topicRoutes.resolveIfRegistered(record.getEventType());
        if (expectedTopic.isEmpty()) {
            markDead(record, "未知逻辑事件类型: " + record.getEventType());
            return;
        }
        if (!expectedTopic.get().equals(record.getTopic())) {
            markDead(record, "Outbox 记录 topic 与注册路由不一致: stored=" + record.getTopic()
                    + ", expected=" + expectedTopic.get());
            return;
        }
        try {
            kafkaTemplate.send(record.getTopic(), record.getMessageKey(), record.getPayload())
                    .get(30, TimeUnit.SECONDS);
            outboxRepository.markPublished(record.getId());
            log.info("Outbox 发布成功: eventId={}, topic={}, key={}", record.getEventId(), record.getTopic(),
                    record.getMessageKey());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleFailure(record, e);
        } catch (Exception e) {
            handleFailure(record, e);
        }
    }

    private void handleFailure(OutboxRecord record, Exception e) {
        int retryCount = record.getRetryCount() == null ? 1 : record.getRetryCount() + 1;
        String error = truncate(e.getMessage());
        if (retryCount >= outboxProperties.getMaxRetry()) {
            markDead(record, error);
            return;
        }
        Date nextRetryTime = new Date(System.currentTimeMillis()
                + outboxProperties.getRetryIntervalSeconds() * 1000L);
        outboxRepository.markRetryable(record.getId(), retryCount, nextRetryTime, error);
        log.warn("Outbox 发布失败，安排重试: eventId={}, retryCount={}, nextRetryTime={}, error={}",
                record.getEventId(), retryCount, nextRetryTime, error);
    }

    private void markDead(OutboxRecord record, String error) {
        outboxRepository.markDead(record.getId(), truncate(error));
        log.error("Outbox 记录置 DEAD: eventId={}, error={}", record.getEventId(), error);
    }

    private String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH - 3) + "...";
    }
}
