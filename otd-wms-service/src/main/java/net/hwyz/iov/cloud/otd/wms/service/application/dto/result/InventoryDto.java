package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import lombok.Builder;

import java.util.Date;

/**
 * 当前库存查询结果
 *
 * @author hwyz_leo
 */
@Builder
public record InventoryDto(Long id, String vin, String buildConfigCode, String warehouseCode,
                           String warehouseName, String warehouseLevel,
                           String storageAreaCode, String storageLocationCode,
                           Integer state, String stateName, String reservedOrderNum,
                           Date inboundTime, String inboundBy, Date outboundTime, String outboundBy,
                           Integer rowVersion, Date createTime) {
}
