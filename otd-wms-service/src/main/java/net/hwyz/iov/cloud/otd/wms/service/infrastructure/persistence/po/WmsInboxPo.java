package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

import java.util.Date;

/**
 * <p>
 * WMS 消费消息接收与幂等记录 数据对象
 * </p>
 *
 * @author hwyz_leo
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_wms_inbox")
public class WmsInboxPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消费者名称
     */
    @TableField("consumer_name")
    private String consumerName;

    /**
     * 来源 Topic
     */
    @TableField("topic")
    private String topic;

    /**
     * 事件唯一 ID
     */
    @TableField("event_id")
    private String eventId;

    /**
     * 事件类型
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 规范化载荷摘要
     */
    @TableField("payload_digest")
    private String payloadDigest;

    /**
     * 处理状态
     */
    @TableField("process_state")
    private String processState;

    /**
     * 业务幂等键
     */
    @TableField("business_key")
    private String businessKey;

    /**
     * 对应结果事件 ID
     */
    @TableField("result_event_id")
    private String resultEventId;

    /**
     * 错误码
     */
    @TableField("error_code")
    private String errorCode;

    /**
     * 脱敏错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 接收时间
     */
    @TableField("received_time")
    private Date receivedTime;

    /**
     * 处理完成时间
     */
    @TableField("processed_time")
    private Date processedTime;
}
