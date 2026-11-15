package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import lombok.Builder;

import java.util.Date;

/**
 * 移储查询结果
 *
 * @author hwyz_leo
 */
@Builder
public record InventoryTransferDto(Long id, String requestId, String vin, String buildConfigCode,
                                   String warehouseCode, String warehouseName, String warehouseLevel,
                                   String oldStorageAreaCode, String oldStorageLocationCode,
                                   String newStorageAreaCode, String newStorageLocationCode,
                                   Date transferTime, String transferBy, Date createTime) {
}
