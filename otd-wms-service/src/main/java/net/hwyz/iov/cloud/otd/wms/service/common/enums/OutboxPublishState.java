package net.hwyz.iov.cloud.otd.wms.service.common.enums;

/**
 * Outbox 发布状态
 *
 * @author hwyz_leo
 */
public enum OutboxPublishState {

    /**
     * 待发布
     */
    PENDING,
    /**
     * 发布失败，等待重试
     */
    FAILED_RETRYABLE,
    /**
     * Broker 已确认
     */
    PUBLISHED,
    /**
     * 超过重试上限或无法路由
     */
    DEAD
}
