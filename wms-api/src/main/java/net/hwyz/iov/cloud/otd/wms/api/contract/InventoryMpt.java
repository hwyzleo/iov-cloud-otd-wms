package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

import java.util.Date;

/**
 * 管理后台库存
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 车架号
     */
    private String vin;

    /**
     * 车型配置代码
     */
    private String modelConfigCode;

    /**
     * 仓库代码
     */
    private String warehouseCode;

    /**
     * 仓库体系层级
     */
    private String warehouseLevel;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 储区代码
     */
    private String storageAreaCode;

    /**
     * 储位代码
     */
    private String storageLocationCode;

    /**
     * 入库时间
     */
    private Date inboundTime;

    /**
     * 入库者
     */
    private Long inboundBy;

    /**
     * 出库时间
     */
    private Date outboundTime;

    /**
     * 出库者
     */
    private Long outboundBy;

    /**
     * 库存状态：1-在库，2-出库
     */
    private Integer state;

    /**
     * 创建时间
     */
    private Date createTime;
}
