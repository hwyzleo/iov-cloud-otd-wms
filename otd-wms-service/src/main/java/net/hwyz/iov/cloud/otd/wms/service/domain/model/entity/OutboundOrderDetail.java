package net.hwyz.iov.cloud.otd.wms.service.domain.model.entity;

import lombok.Builder;
import lombok.Getter;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundDetailState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;

import java.util.Date;

/**
 * 出库单 VIN 明细实体
 *
 * @author hwyz_leo
 */
@Getter
@Builder
public class OutboundOrderDetail {

    /**
     * 主键
     */
    private Long id;
    /**
     * WMS 出库单号
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
     * 预留时仓库快照
     */
    private String warehouseCode;
    /**
     * 预留时储区快照
     */
    private String storageAreaCode;
    /**
     * 预留时储位快照
     */
    private String storageLocationCode;
    /**
     * 明细状态
     */
    private OutboundDetailState state;
    /**
     * 预留时间
     */
    private Date reserveTime;
    /**
     * 预留者
     */
    private String reserveBy;
    /**
     * 装车时间
     */
    private Date loadTime;
    /**
     * 装车者
     */
    private String loadBy;
    /**
     * 出库时间
     */
    private Date outboundTime;
    /**
     * 出库者
     */
    private String outboundBy;
    /**
     * 释放时间
     */
    private Date releaseTime;
    /**
     * 释放者
     */
    private String releaseBy;
    /**
     * 记录版本
     */
    private Integer rowVersion;

    /**
     * 装车：RESERVED → LOADED
     *
     * @param operator 可信操作人
     * @param now      WMS 生成的装车时间
     */
    public void markLoaded(String operator, Date now) {
        if (state != OutboundDetailState.RESERVED) {
            throw WmsBusinessException.of(WmsErrorCode.OUTBOUND_STATE_CONFLICT,
                    "车辆[" + vin + "]明细状态为[" + state + "]，不允许装车");
        }
        this.state = OutboundDetailState.LOADED;
        this.loadTime = now;
        this.loadBy = operator;
    }

    /**
     * 出库：LOADED → OUTBOUND
     *
     * @param operator 可信操作人
     * @param now      WMS 生成的出库时间
     */
    public void markOutbound(String operator, Date now) {
        if (state != OutboundDetailState.LOADED) {
            throw WmsBusinessException.of(WmsErrorCode.OUTBOUND_STATE_CONFLICT,
                    "车辆[" + vin + "]明细状态为[" + state + "]，不允许出库");
        }
        this.state = OutboundDetailState.OUTBOUND;
        this.outboundTime = now;
        this.outboundBy = operator;
    }

    /**
     * 取消释放：RESERVED → RELEASED
     *
     * @param operator 可信操作人
     * @param now      WMS 生成的释放时间
     */
    public void markReleased(String operator, Date now) {
        if (state != OutboundDetailState.RESERVED) {
            throw WmsBusinessException.of(WmsErrorCode.OUTBOUND_STATE_CONFLICT,
                    "车辆[" + vin + "]明细状态为[" + state + "]，不允许取消释放");
        }
        this.state = OutboundDetailState.RELEASED;
        this.releaseTime = now;
        this.releaseBy = operator;
    }
}
