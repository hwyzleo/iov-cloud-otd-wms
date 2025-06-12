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
 * 库存 数据对象
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
@TableName("tb_inventory")
public class InventoryPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    /**
     * 出库时间
     */
    @TableField("outbound_time")
    private Date outboundTime;

    /**
     * 出库者
     */
    @TableField("outbound_by")
    private Long outboundBy;

    /**
     * 库存状态：1-在库，2-出库
     */
    @TableField("state")
    private Integer state;
}
