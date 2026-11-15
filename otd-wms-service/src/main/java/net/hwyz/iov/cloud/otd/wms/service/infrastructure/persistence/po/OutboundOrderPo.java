package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

/**
 * <p>
 * 出库单 数据对象
 * </p>
 *
 * @author hwyz_leo
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_outbound_order")
public class OutboundOrderPo extends BasePo {

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
     * 首次出库请求幂等标识
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
     * 已解析仓库代码
     */
    @TableField("warehouse_code")
    private String warehouseCode;

    /**
     * 出库单状态
     */
    @TableField("state")
    private String state;

    /**
     * 出库聚合版本
     */
    @TableField("aggregate_version")
    private Long aggregateVersion;
}
