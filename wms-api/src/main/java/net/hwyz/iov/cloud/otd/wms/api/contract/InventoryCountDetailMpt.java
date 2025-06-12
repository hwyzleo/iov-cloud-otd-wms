package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

import java.util.Date;

/**
 * 管理后台盘点信息
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryCountDetailMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 盘点单号
     */
    private String orderNum;

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
     * 储区代码
     */
    private String storageAreaCode;

    /**
     * 储位代码
     */
    private String storageLocationCode;

    /**
     * 实际仓库代码
     */
    private String actualWarehouseCode;

    /**
     * 实际储区代码
     */
    private String actualStorageAreaCode;

    /**
     * 实际储位代码
     */
    private String actualStorageLocationCode;

    /**
     * 盘点时间
     */
    private Date countTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
