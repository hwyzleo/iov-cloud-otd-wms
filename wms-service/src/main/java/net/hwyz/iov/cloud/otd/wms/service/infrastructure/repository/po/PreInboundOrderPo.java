package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

import java.util.Date;

/**
 * <p>
 * 预入库单 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-06
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_pre_inbound_order")
public class PreInboundOrderPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 预入库单号
     */
    @TableField("order_num")
    private String orderNum;

    /**
     * 车架号
     */
    @TableField("vin")
    private String vin;

    /**
     * 车型配置代码
     */
    @TableField("model_config_code")
    private String modelConfigCode;

    /**
     * 仓库代码
     */
    @TableField("warehouse_code")
    private String warehouseCode;

    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
     * 仓库层级
     */
    @TableField("warehouse_level")
    private String warehouseLevel;

    /**
     * 预计到达时间
     */
    @TableField("estimated_arrival_time")
    private Date estimatedArrivalTime;

    /**
     * 预计入库时间
     */
    @TableField("estimated_inbound_time")
    private Date estimatedInboundTime;

    /**
     * 是否已审核
     */
    @TableField("audit")
    private Boolean audit;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private Date auditTime;

    /**
     * 审核者
     */
    @TableField("audit_by")
    private Long auditBy;

    /**
     * 是否已到达
     */
    @TableField("arrival")
    private Boolean arrival;

    /**
     * 到达时间
     */
    @TableField("arrival_time")
    private Date arrivalTime;

    /**
     * 是否已入库
     */
    @TableField("inbound")
    private Boolean inbound;

    /**
     * 入库时间
     */
    @TableField("inbound_time")
    private Date inboundTime;

    /**
     * 入库者
     */
    @TableField("inbound_by")
    private Long inboundBy;
}
