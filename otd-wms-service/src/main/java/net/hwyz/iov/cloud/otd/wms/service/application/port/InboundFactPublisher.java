package net.hwyz.iov.cloud.otd.wms.service.application.port;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ItemReasonDto;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.InboundTask;

import java.util.List;

/**
 * 入库事实事件发布端口（应用层定义，基础设施在同一本地事务内写入 Outbox）
 *
 * @author hwyz_leo
 */
public interface InboundFactPublisher {

    /**
     * 发布待入库任务事实（VehicleInboundChangedEvent）
     *
     * @param task      待入库任务
     * @param requestId 导致本次流转的请求标识
     */
    void publishChanged(InboundTask task, String requestId);

    /**
     * 发布到货命令拒绝事实（VehicleInboundCommandRejectedEvent）
     *
     * @param requestId      请求标识
     * @param sourceSystem   来源系统
     * @param sourceOrderNum 来源单号
     * @param inboundTaskNum WMS 任务号（无任务时为空）
     * @param reasonCode     拒绝原因错误码
     * @param itemReasons    逐 VIN 原因
     * @param aggregateVersion 当前聚合版本（无任务时为 0）
     */
    void publishRejected(String requestId, String sourceSystem, String sourceOrderNum, String inboundTaskNum,
                         String reasonCode, List<ItemReasonDto> itemReasons, long aggregateVersion);
}
