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
 * 移库 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-12
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_inventory_transfer")
public class InventoryTransferPo extends BasePo {

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
     * 原储区代码
     */
    @TableField("old_storage_area_code")
    private String oldStorageAreaCode;

    /**
     * 原储位代码
     */
    @TableField("old_storage_location_code")
    private String oldStorageLocationCode;

    /**
     * 新储区代码
     */
    @TableField("new_storage_area_code")
    private String newStorageAreaCode;

    /**
     * 新储位代码
     */
    @TableField("new_storage_location_code")
    private String newStorageLocationCode;

    /**
     * 移库状态
     */
    @TableField("transfer_state")
    private Short transferState;

    /**
     * 移库时间
     */
    @TableField("transfer_time")
    private Date transferTime;

    /**
     * 移库者
     */
    @TableField("transfer_by")
    private Long transferBy;
}
