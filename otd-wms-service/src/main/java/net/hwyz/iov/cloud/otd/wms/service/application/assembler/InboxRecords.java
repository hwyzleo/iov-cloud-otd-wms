package net.hwyz.iov.cloud.otd.wms.service.application.assembler;

import net.hwyz.iov.cloud.otd.wms.service.common.enums.InboxProcessState;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;

import java.util.Date;

/**
 * Inbox 记录装配
 * <p>
 * 消费者名称是协议身份（稳定值），Topic 来自类型化配置；处理结果与业务写入同一本地事务提交。
 *
 * @author hwyz_leo
 */
public final class InboxRecords {

    /**
     * VMD 生产事件消费者名称
     */
    public static final String VMD_PRODUCE_CONSUMER = "otd-wms-vehicle-produce";
    /**
     * 出库命令消费者名称
     */
    public static final String OUTBOUND_COMMAND_CONSUMER = "otd-wms-vehicle-outbound-command";
    /**
     * 到货命令消费者名称
     */
    public static final String INBOUND_COMMAND_CONSUMER = "otd-wms-vehicle-inbound-command";
    /**
     * MPT 业务动作幂等消费者名称
     */
    public static final String MPT_ACTION_CONSUMER = "otd-wms-mpt-action";
    /**
     * MPT 业务动作逻辑 Topic
     */
    public static final String MPT_TOPIC = "wms.mpt.action";

    private InboxRecords() {
    }

    /**
     * 处理成功记录
     *
     * @param consumerName  消费者名称
     * @param topic         来源 Topic
     * @param eventId       事件 ID
     * @param eventType     事件类型
     * @param payloadDigest 载荷摘要
     * @param businessKey   业务键
     * @param resultEventId 结果事件 ID
     * @return Inbox 记录
     */
    public static InboxRecord processed(String consumerName, String topic, String eventId, String eventType,
                                        String payloadDigest, String businessKey, String resultEventId) {
        return build(consumerName, topic, eventId, eventType, payloadDigest, businessKey,
                InboxProcessState.PROCESSED, resultEventId, null, null);
    }

    /**
     * 合法业务拒绝记录
     *
     * @param consumerName  消费者名称
     * @param topic         来源 Topic
     * @param eventId       事件 ID
     * @param eventType     事件类型
     * @param payloadDigest 载荷摘要
     * @param businessKey   业务键
     * @param resultEventId 拒绝事件 ID
     * @param reasonCode    拒绝原因错误码
     * @return Inbox 记录
     */
    public static InboxRecord businessRejected(String consumerName, String topic, String eventId, String eventType,
                                               String payloadDigest, String businessKey, String resultEventId,
                                               String reasonCode) {
        return build(consumerName, topic, eventId, eventType, payloadDigest, businessKey,
                InboxProcessState.BUSINESS_REJECTED, resultEventId, reasonCode, null);
    }

    /**
     * 确定性冲突死信记录
     *
     * @param consumerName  消费者名称
     * @param topic         来源 Topic
     * @param eventId       事件 ID
     * @param eventType     事件类型
     * @param payloadDigest 载荷摘要
     * @param businessKey   业务键
     * @param reasonCode    错误码
     * @param message       脱敏说明
     * @return Inbox 记录
     */
    public static InboxRecord deadLettered(String consumerName, String topic, String eventId, String eventType,
                                           String payloadDigest, String businessKey, String reasonCode, String message) {
        return build(consumerName, topic, eventId, eventType, payloadDigest, businessKey,
                InboxProcessState.DEAD_LETTERED, null, reasonCode, message);
    }

    private static InboxRecord build(String consumerName, String topic, String eventId, String eventType,
                                     String payloadDigest, String businessKey, InboxProcessState state,
                                     String resultEventId, String errorCode, String errorMessage) {
        Date now = new Date();
        return InboxRecord.builder()
                .consumerName(consumerName)
                .topic(topic)
                .eventId(eventId)
                .eventType(eventType == null ? "UNKNOWN" : eventType)
                .payloadDigest(payloadDigest == null ? "" : payloadDigest)
                .processState(state)
                .businessKey(businessKey)
                .resultEventId(resultEventId)
                .errorCode(errorCode)
                .errorMessage(truncate(errorMessage))
                .receivedTime(now)
                .processedTime(now)
                .build();
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 2000 ? message : message.substring(0, 1997) + "...";
    }
}
