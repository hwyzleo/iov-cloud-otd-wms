package net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate;

import lombok.Builder;
import lombok.Getter;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.PreInboundState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;

import java.util.Date;

/**
 * 预入库单聚合根
 * <p>
 * {@code state} 是预入库状态的唯一权威；到达／入库时间与操作人由 WMS 生成。
 *
 * @author hwyz_leo
 */
@Getter
@Builder
public class PreInboundOrder {

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
     * 生产配置代码
     */
    private String buildConfigCode;
    /**
     * 仓库代码
     */
    private String warehouseCode;
    /**
     * 仓库名称（查询投影）
     */
    private String warehouseName;
    /**
     * 仓库体系层级（查询投影）
     */
    private String warehouseLevel;
    /**
     * 权威状态
     */
    private PreInboundState state;
    /**
     * 来源系统
     */
    private String sourceSystem;
    /**
     * 首个来源事件 ID
     */
    private String sourceEventId;
    /**
     * 来源批次号
     */
    private String sourceBatchNum;
    /**
     * 预计到达时间
     */
    private Date estimatedArrivalTime;
    /**
     * 预计入库时间
     */
    private Date estimatedInboundTime;
    /**
     * 到达时间
     */
    private Date arrivalTime;
    /**
     * 入库时间
     */
    private Date inboundTime;
    /**
     * 入库者
     */
    private String inboundBy;
    /**
     * 记录版本
     */
    private Integer rowVersion;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 确认到达：PENDING_ARRIVAL → ARRIVED
     *
     * @param now WMS 生成的到达时间
     */
    public void confirmArrival(Date now) {
        if (state != PreInboundState.PENDING_ARRIVAL) {
            throw WmsBusinessException.of(WmsErrorCode.PRE_INBOUND_STATE_CONFLICT,
                    "预入库单[" + orderNum + "]当前状态为[" + state + "]，不允许确认到达");
        }
        this.state = PreInboundState.ARRIVED;
        this.arrivalTime = now;
    }

    /**
     * 确认入库：ARRIVED → COMPLETED
     *
     * @param operator 可信操作人
     * @param now      WMS 生成的入库时间
     */
    public void confirmInbound(String operator, Date now) {
        if (state != PreInboundState.ARRIVED) {
            throw WmsBusinessException.of(WmsErrorCode.PRE_INBOUND_STATE_CONFLICT,
                    "预入库单[" + orderNum + "]当前状态为[" + state + "]，不允许确认入库");
        }
        this.state = PreInboundState.COMPLETED;
        this.inboundTime = now;
        this.inboundBy = operator;
    }

    /**
     * 取消：仅 PENDING_ARRIVAL / ARRIVED 允许，不创建库存
     */
    public void cancel() {
        if (state != PreInboundState.PENDING_ARRIVAL && state != PreInboundState.ARRIVED) {
            throw WmsBusinessException.of(WmsErrorCode.PRE_INBOUND_STATE_CONFLICT,
                    "预入库单[" + orderNum + "]当前状态为[" + state + "]，不允许取消");
        }
        this.state = PreInboundState.CANCELLED;
    }

    /**
     * 是否为到达状态（入库前置门禁）
     *
     * @return 是否可入库
     */
    public boolean isArrived() {
        return state == PreInboundState.ARRIVED;
    }
}
