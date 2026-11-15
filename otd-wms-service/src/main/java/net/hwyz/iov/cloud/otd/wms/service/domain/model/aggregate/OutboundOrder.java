package net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate;

import lombok.Builder;
import lombok.Getter;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundDetailState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundOrderState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 出库单聚合根
 * <p>
 * 每次接受的预留、取消、装车或出库流转递增 {@code aggregateVersion}；业务拒绝不推进版本。
 *
 * @author hwyz_leo
 */
@Getter
@Builder
public class OutboundOrder {

    /**
     * 主键
     */
    private Long id;
    /**
     * WMS 出库单号
     */
    private String orderNum;
    /**
     * 首次出库请求幂等标识
     */
    private String requestId;
    /**
     * 通用来源系统
     */
    private String sourceSystem;
    /**
     * 外部来源单号
     */
    private String sourceOrderNum;
    /**
     * 已解析仓库代码
     */
    private String warehouseCode;
    /**
     * 出库单状态
     */
    private OutboundOrderState state;
    /**
     * 出库聚合版本
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
     * VIN 明细
     */
    @Builder.Default
    private List<OutboundOrderDetail> details = new ArrayList<>();

    /**
     * 创建已预留状态的出库单（全部明细 RESERVED）
     *
     * @param requestId  首次请求标识
     * @param sourceSystem 来源系统
     * @param sourceOrderNum 来源单号
     * @param warehouseCode 仓库代码
     * @param orderNum 出库单号
     * @param details 明细
     * @return 出库单聚合
     */
    public static OutboundOrder createReserved(String requestId, String sourceSystem, String sourceOrderNum,
                                               String warehouseCode, String orderNum,
                                               List<OutboundOrderDetail> details) {
        return OutboundOrder.builder()
                .orderNum(orderNum)
                .requestId(requestId)
                .sourceSystem(sourceSystem)
                .sourceOrderNum(sourceOrderNum)
                .warehouseCode(warehouseCode)
                .state(OutboundOrderState.RESERVED)
                .aggregateVersion(1L)
                .rowVersion(1)
                .details(new ArrayList<>(details))
                .build();
    }

    /**
     * 装车：明细 RESERVED → LOADED，重算订单状态并递增聚合版本
     *
     * @param vin      车架号
     * @param operator 可信操作人
     * @param now      WMS 生成的装车时间
     * @return 流转后的明细
     */
    public OutboundOrderDetail loadVin(String vin, String operator, Date now) {
        OutboundOrderDetail detail = requireDetail(vin);
        detail.markLoaded(operator, now);
        refreshState();
        return detail;
    }

    /**
     * 出库：明细 LOADED → OUTBOUND，重算订单状态并递增聚合版本
     *
     * @param vin      车架号
     * @param operator 可信操作人
     * @param now      WMS 生成的出库时间
     * @return 流转后的明细
     */
    public OutboundOrderDetail outboundVin(String vin, String operator, Date now) {
        OutboundOrderDetail detail = requireDetail(vin);
        detail.markOutbound(operator, now);
        refreshState();
        return detail;
    }

    /**
     * 取消订单：仅当全部明细均为 RESERVED 时允许，明细 RESERVED → RELEASED
     *
     * @param operator 可信操作人
     * @param now      WMS 生成的释放时间
     */
    public void cancel(String operator, Date now) {
        if (state == OutboundOrderState.CANCELLED || state == OutboundOrderState.COMPLETED) {
            throw WmsBusinessException.of(WmsErrorCode.OUTBOUND_STATE_CONFLICT,
                    "出库单[" + orderNum + "]当前状态为[" + state + "]，不允许取消");
        }
        for (OutboundOrderDetail detail : details) {
            if (detail.getState() != OutboundDetailState.RESERVED) {
                throw WmsBusinessException.of(WmsErrorCode.OUTBOUND_STATE_CONFLICT,
                        "出库单[" + orderNum + "]车辆[" + detail.getVin() + "]已装车或已出库，禁止取消");
            }
        }
        for (OutboundOrderDetail detail : details) {
            detail.markReleased(operator, now);
        }
        refreshState();
    }

    /**
     * 重算订单状态并递增聚合版本
     */
    private void refreshState() {
        long outbound = details.stream().filter(d -> d.getState() == OutboundDetailState.OUTBOUND).count();
        long loaded = details.stream().filter(d -> d.getState() == OutboundDetailState.LOADED).count();
        long reserved = details.stream().filter(d -> d.getState() == OutboundDetailState.RESERVED).count();
        long released = details.stream().filter(d -> d.getState() == OutboundDetailState.RELEASED).count();
        int total = details.size();

        if (released == total) {
            this.state = OutboundOrderState.CANCELLED;
        } else if (outbound == total) {
            this.state = OutboundOrderState.COMPLETED;
        } else if (outbound > 0) {
            this.state = OutboundOrderState.PARTIALLY_OUTBOUND;
        } else if (loaded == total) {
            this.state = OutboundOrderState.LOADED;
        } else if (loaded > 0) {
            this.state = OutboundOrderState.PARTIALLY_LOADED;
        } else if (reserved == total) {
            this.state = OutboundOrderState.RESERVED;
        } else {
            this.state = OutboundOrderState.PARTIALLY_OUTBOUND;
        }
        this.aggregateVersion = this.aggregateVersion == null ? 1L : this.aggregateVersion + 1;
    }

    private OutboundOrderDetail requireDetail(String vin) {
        return details.stream()
                .filter(detail -> detail.getVin().equals(vin))
                .findFirst()
                .orElseThrow(() -> WmsBusinessException.of(WmsErrorCode.OUTBOUND_STATE_CONFLICT,
                        "出库单[" + orderNum + "]不存在车辆[" + vin + "]明细"));
    }
}
