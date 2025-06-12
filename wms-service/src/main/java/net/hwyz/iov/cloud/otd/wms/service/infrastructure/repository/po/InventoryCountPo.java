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
 * 盘点 数据对象
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
@TableName("tb_inventory_count")
public class InventoryCountPo extends BasePo {

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
     * 盘点类型：1-按仓库，2-按储区
     */
    @TableField("type")
    private Integer type;

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
     * 盘点状态：1-新建，2-已下发，3-开始盘点，4-结束盘点
     */
    @TableField("state")
    private Integer state;

    /**
     * 开始盘点时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 结束盘点时间
     */
    @TableField("end_time")
    private Date endTime;
}
