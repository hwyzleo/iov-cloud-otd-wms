package net.hwyz.iov.cloud.otd.wms.service.domain.model.entity;

import lombok.Builder;
import lombok.Getter;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InboxProcessState;

import java.util.Date;

/**
 * Inbox 消费幂等记录
 *
 * @author hwyz_leo
 */
@Getter
@Builder
public class InboxRecord {

    /**
     * 消费者名称
     */
    private String consumerName;
    /**
     * 来源 Topic
     */
    private String topic;
    /**
     * 事件唯一 ID
     */
    private String eventId;
    /**
     * 事件类型
     */
    private String eventType;
    /**
     * 规范化载荷摘要
     */
    private String payloadDigest;
    /**
     * 处理状态
     */
    private InboxProcessState processState;
    /**
     * 业务幂等键
     */
    private String businessKey;
    /**
     * 对应结果事件 ID
     */
    private String resultEventId;
    /**
     * 错误码
     */
    private String errorCode;
    /**
     * 脱敏错误信息
     */
    private String errorMessage;
    /**
     * 接收时间
     */
    private Date receivedTime;
    /**
     * 处理完成时间
     */
    private Date processedTime;
}
