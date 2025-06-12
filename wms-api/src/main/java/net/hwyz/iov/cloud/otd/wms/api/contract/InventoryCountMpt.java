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
public class InventoryCountMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 盘点单号
     */
    private String orderNum;

    /**
     * 盘点类型：1-按仓库，2-按储区
     */
    private Short type;

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
     * 储区代码
     */
    private String storageAreaCode;

    /**
     * 盘点状态：1-新建，2-已下发，3-开始盘点，4-结束盘点
     */
    private Integer state;

    /**
     * 开始盘点时间
     */
    private Date startTime;

    /**
     * 结束盘点时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
