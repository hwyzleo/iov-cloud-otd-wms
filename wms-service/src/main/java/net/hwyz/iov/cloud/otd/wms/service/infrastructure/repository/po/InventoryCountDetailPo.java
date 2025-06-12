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
 * 盘点明细 数据对象
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
@TableName("tb_inventory_count_detail")
public class InventoryCountDetailPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 盘点单号
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
     * 实际仓库代码
     */
    @TableField("actual_warehouse_code")
    private String actualWarehouseCode;

    /**
     * 实际储区代码
     */
    @TableField("actual_storage_area_code")
    private String actualStorageAreaCode;

    /**
     * 实际储位代码
     */
    @TableField("actual_storage_location_code")
    private String actualStorageLocationCode;

    /**
     * 盘点时间
     */
    @TableField("count_time")
    private Date countTime;
}
