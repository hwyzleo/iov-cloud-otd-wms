package net.hwyz.iov.cloud.otd.wms.service.domain.model.entity;

import lombok.Builder;
import lombok.Getter;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboxPublishState;

import java.util.Date;

/**
 * Outbox 事务性事件发布记录
 *
 * @author hwyz_leo
 */
@Getter
@Builder
public class OutboxRecord {

    /**
     * 主键
     */
    private Long id;
    /**
     * 事件唯一 ID
     */
    private String eventId;
    /**
     * 事件类型
     */
    private String eventType;
    /**
     * 聚合类型
     */
    private String aggregateType;
    /**
     * 聚合 ID
     */
    private String aggregateId;
    /**
     * 聚合版本
     */
    private Long aggregateVersion;
    /**
     * 注册的目标 Topic
     */
    private String topic;
    /**
     * Kafka Key
     */
    private String messageKey;
    /**
     * 不可变 JSON 载荷
     */
    private String payload;
    /**
     * 发布状态
     */
    private OutboxPublishState publishState;
    /**
     * 已重试次数
     */
    private Integer retryCount;
    /**
     * 下次重试时间
     */
    private Date nextRetryTime;
    /**
     * Broker 确认时间
     */
    private Date publishedAt;
    /**
     * 脱敏的最后错误
     */
    private String lastError;
}
