package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import lombok.Builder;

import java.util.Date;

/**
 * 预入库单查询结果
 *
 * @author hwyz_leo
 */
@Builder
public record PreInboundOrderDto(Long id, String orderNum, String vin, String buildConfigCode,
                                 String warehouseCode, String warehouseName, String warehouseLevel,
                                 String state, String sourceSystem, String sourceEventId, String sourceBatchNum,
                                 Date estimatedArrivalTime, Date estimatedInboundTime, Date arrivalTime,
                                 Date inboundTime, Integer rowVersion, Date createTime) {
}
