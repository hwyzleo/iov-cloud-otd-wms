package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import lombok.Builder;

import java.util.Date;

/**
 * 待入库任务查询结果
 *
 * @author hwyz_leo
 */
@Builder
public record InboundTaskDto(Long id, String orderNum, String requestId, String sourceSystem,
                             String sourceOrderNum, String vin, String buildConfigCode, String warehouseCode,
                             String state, Date estimatedArrivalTime, Date arrivalTime, Date inboundTime,
                             Date cancelTime, String cancelReason, Integer lifecycleSeq, Long aggregateVersion,
                             Integer rowVersion, Date createTime) {
}
