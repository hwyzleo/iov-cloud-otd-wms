package net.hwyz.iov.cloud.otd.wms.service.common.enums;

/**
 * 消息消费结果
 *
 * @author hwyz_leo
 */
public enum ConsumeOutcome {

    /**
     * 业务处理成功（或等价补发被复用）
     */
    PROCESSED,
    /**
     * 重复事件，返回已记录结果
     */
    DUPLICATE,
    /**
     * 合法业务拒绝，结果为拒绝事实
     */
    BUSINESS_REJECTED,
    /**
     * 确定性冲突，不重试并进入死信
     */
    DEAD_LETTER
}
