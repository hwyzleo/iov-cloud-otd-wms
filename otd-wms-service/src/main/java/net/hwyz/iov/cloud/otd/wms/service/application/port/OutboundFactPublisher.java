package net.hwyz.iov.cloud.otd.wms.service.application.port;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ItemReasonDto;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;

import java.util.List;

/**
 * 出库事实事件发布端口（应用层定义，基础设施在同一本地事务内写入 Outbox）
 *
 * @author hwyz_leo
 */
public interface OutboundFactPublisher {

    /**
     * 发布出库单事实（VehicleOutboundChangedEvent）
     *
     * @param order     出库单聚合
     * @param requestId 导致本次流转的请求标识
     */
    void publishChanged(OutboundOrder order, String requestId);

    /**
     * 发布出库命令拒绝事实（VehicleOutboundCommandRejectedEvent）
     *
     * @param requestId        请求标识
     * @param sourceSystem     来源系统
     * @param sourceOrderNum   来源单号
     * @param outboundOrderNum WMS 出库单号（无订单时为空）
     * @param reasonCode       拒绝原因错误码
     * @param itemReasons      逐 VIN 原因
     * @param aggregateVersion 当前聚合版本（无订单时为 0）
     */
    void publishRejected(String requestId, String sourceSystem, String sourceOrderNum, String outboundOrderNum,
                         String reasonCode, List<ItemReasonDto> itemReasons, long aggregateVersion);
}
