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
 * 出库单 VIN 明细 数据对象
 * </p>
 *
 * @author hwyz_leo
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_outbound_order_detail")
public class OutboundOrderDetailPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * WMS 出库单号
     */
    @TableField("order_num")
    private String orderNum;

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
     * 预留时仓库快照
     */
    @TableField("warehouse_code")
    private String warehouseCode;

    /**
     * 预留时储区快照
     */
    @TableField("storage_area_code")
    private String storageAreaCode;

    /**
     * 预留时储位快照
     */
    @TableField("storage_location_code")
    private String storageLocationCode;

    /**
     * 明细状态
     */
    @TableField("state")
    private String state;

    /**
     * 预留时间
     */
    @TableField("reserve_time")
    private Date reserveTime;

    /**
     * 预留者
     */
    @TableField("reserve_by")
    private Long reserveBy;

    /**
     * 装车时间
     */
    @TableField("load_time")
    private Date loadTime;

    /**
     * 装车者
     */
    @TableField("load_by")
    private Long loadBy;

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
     * 取消释放时间
     */
    @TableField("release_time")
    private Date releaseTime;

    /**
     * 取消释放者
     */
    @TableField("release_by")
    private Long releaseBy;
}
