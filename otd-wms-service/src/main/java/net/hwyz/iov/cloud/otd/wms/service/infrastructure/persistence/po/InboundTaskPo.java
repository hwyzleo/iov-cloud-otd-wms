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
 * 交付库待入库任务 数据对象
 * </p>
 *
 * @author hwyz_leo
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_inbound_task")
public class InboundTaskPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 待入库任务号
     */
    @TableField("order_num")
    private String orderNum;

    /**
     * 到货请求幂等标识
     */
    @TableField("request_id")
    private String requestId;

    /**
     * 通用来源系统
     */
    @TableField("source_system")
    private String sourceSystem;

    /**
     * 来源单号
     */
    @TableField("source_order_num")
    private String sourceOrderNum;

    /**
     * 车架号
     */
    @TableField("vin")
    private String vin;

    /**
     * 生产配置代码
     */
    @TableField("build_config_code")
    private String buildConfigCode;

    /**
     * 目标交付库代码
     */
    @TableField("warehouse_code")
    private String warehouseCode;

    /**
     * 任务状态
     */
    @TableField("state")
    private String state;

    /**
     * 预计到达时间
     */
    @TableField("estimated_arrival_time")
    private Date estimatedArrivalTime;

    /**
     * 到达时间
     */
    @TableField("arrival_time")
    private Date arrivalTime;

    /**
     * 到达确认人
     */
    @TableField("arrival_by")
    private Long arrivalBy;

    /**
     * 入库完成时间
     */
    @TableField("inbound_time")
    private Date inboundTime;

    /**
     * 入库确认人
     */
    @TableField("inbound_by")
    private Long inboundBy;

    /**
     * 取消时间
     */
    @TableField("cancel_time")
    private Date cancelTime;

    /**
     * 取消人
     */
    @TableField("cancel_by")
    private Long cancelBy;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 目标生命周期段次
     */
    @TableField("lifecycle_seq")
    private Integer lifecycleSeq;

    /**
     * 任务聚合版本
     */
    @TableField("aggregate_version")
    private Long aggregateVersion;
}
