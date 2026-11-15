package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import lombok.Builder;

import java.util.Date;

/**
 * 出库单 VIN 明细查询结果
 *
 * @author hwyz_leo
 */
@Builder
public record OutboundOrderDetailDto(Long id, String orderNum, String vin, String buildConfigCode,
                                     String warehouseCode, String storageAreaCode, String storageLocationCode,
                                     String state, Date reserveTime, Date loadTime, Date outboundTime,
                                     Date releaseTime, Integer rowVersion) {
}
