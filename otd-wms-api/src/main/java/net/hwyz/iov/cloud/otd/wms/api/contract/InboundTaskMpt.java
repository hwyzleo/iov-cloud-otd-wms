package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.bean.BaseRequest;

import java.util.Date;

/**
 * 管理后台待入库任务
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InboundTaskMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * WMS 待入库任务号
     */
    private String orderNum;

    /**
     * 到货请求幂等标识
     */
    private String requestId;

    /**
     * 通用来源系统
     */
    private String sourceSystem;

    /**
     * 来源单号
     */
    private String sourceOrderNum;

    /**
     * 车架号
     */
    private String vin;

    /**
     * 生产配置代码
     */
    private String buildConfigCode;

    /**
     * 目标交付库代码
     */
    private String warehouseCode;

    /**
     * 任务状态
     */
    private String state;

    /**
     * 预计到达时间
     */
    private Date estimatedArrivalTime;

    /**
     * 到达时间
     */
    private Date arrivalTime;

    /**
     * 入库时间
     */
    private Date inboundTime;

    /**
     * 取消时间
     */
    private Date cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 目标生命周期段次
     */
    private Integer lifecycleSeq;

    /**
     * 任务聚合版本
     */
    private Long aggregateVersion;

    /**
     * 记录版本
     */
    private Integer rowVersion;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;
}
