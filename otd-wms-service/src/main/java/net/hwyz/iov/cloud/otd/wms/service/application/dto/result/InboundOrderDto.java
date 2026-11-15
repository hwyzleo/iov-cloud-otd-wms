package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import lombok.Builder;

import java.util.Date;

/**
 * 入库单查询结果
 *
 * @author hwyz_leo
 */
@Builder
public record InboundOrderDto(Long id, String orderNum, String preOrderNum, String vin, String buildConfigCode,
                              String warehouseCode, String warehouseName, String warehouseLevel,
                              String storageAreaCode, String storageLocationCode,
                              Date inboundTime, String inboundBy, Date createTime) {
}
