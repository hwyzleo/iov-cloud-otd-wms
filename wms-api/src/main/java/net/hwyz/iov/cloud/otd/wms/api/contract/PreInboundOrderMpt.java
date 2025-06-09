package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

import java.util.Date;

/**
 * 管理后台预入库单
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PreInboundOrderMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 预入库单号
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
     * 预计入库时间
     */
    private Date estimatedInboundTime;

    /**
     * 是否已审核
     */
    private Boolean audit;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 审核者
     */
    private Long auditBy;

    /**
     * 是否已到达
     */
    private Boolean arrival;

    /**
     * 到达时间
     */
    private Date arrivalTime;

    /**
     * 是否已入库
     */
    private Boolean inbound;

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
