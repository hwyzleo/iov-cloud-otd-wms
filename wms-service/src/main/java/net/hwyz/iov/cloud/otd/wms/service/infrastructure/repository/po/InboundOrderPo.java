package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 入库单 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-11
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_inbound_order")
public class InboundOrderPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 入库单号
     */
    @TableField("order_num")
    private String orderNum;

    /**
     * 预入库单号
     */
    @TableField("pre_order_num")
    private String preOrderNum;

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
     * 储区代码
     */
    @TableField("storage_area_code")
    private String storageAreaCode;

    /**
     * 储位代码
     */
    @TableField("storage_location_code")
    private String storageLocationCode;

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
