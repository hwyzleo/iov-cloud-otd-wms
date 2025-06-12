package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

import java.util.Date;

/**
 * 管理后台移库
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryTransferMpt extends BaseRequest {

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
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 仓库层级
     */
    private String warehouseLevel;

    /**
     * 原储区代码
     */
    private String oldStorageAreaCode;

    /**
     * 原储位代码
     */
    private String oldStorageLocationCode;

    /**
     * 新储区代码
     */
    private String newStorageAreaCode;

    /**
     * 新储位代码
     */
    private String newStorageLocationCode;

    /**
     * 移库状态
     */
    private Short transferState;

    /**
     * 移库时间
     */
    private Date transferTime;

    /**
     * 移库者
     */
    private Long transferBy;

    /**
     * 创建时间
     */
    private Date createTime;
}
