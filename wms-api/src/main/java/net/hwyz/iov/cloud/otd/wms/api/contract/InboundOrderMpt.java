package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

import java.util.Date;

/**
 * 管理后台入库单
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InboundOrderMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 入库单号
     */
    private String orderNum;

    /**
     * 预入库单号
     */
    private String preOrderNum;

    /**
     * 车架号
     */
    private String vin;

    /**
     * 车型配置代码
     */
    private String modelConfigCode;

    /**
     * 仓库体系层级
     */
    private String warehouseLevel;

    /**
     * 仓库代码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 入库时间
     */
    private Date inboundTime;

    /**
     * 入库者
     */
    private Long inboundBy;

    /**
     * 创建时间
     */
    private Date createTime;
}
