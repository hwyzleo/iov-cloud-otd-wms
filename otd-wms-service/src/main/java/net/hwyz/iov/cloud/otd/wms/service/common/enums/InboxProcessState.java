package net.hwyz.iov.cloud.otd.wms.service.common.enums;

/**
 * Inbox 处理状态
 *
 * @author hwyz_leo
 */
public enum InboxProcessState {

    /**
     * 业务处理及必要 Outbox 已提交
     */
    PROCESSED,
    /**
     * 合法业务拒绝及拒绝事件已提交
     */
    BUSINESS_REJECTED,
    /**
     * 消息结构或确定性冲突已进入死信处理
     */
    DEAD_LETTERED
}
