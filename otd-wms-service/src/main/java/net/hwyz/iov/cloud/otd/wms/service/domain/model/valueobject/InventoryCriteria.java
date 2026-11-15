package net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject;

import java.util.Date;

/**
 * 库存查询条件（数据库过滤与分页）
 *
 * @author hwyz_leo
 */
public record InventoryCriteria(String vin, String warehouseLevel, String warehouseCode, Integer state,
                                String storageAreaCode, String storageLocationCode,
                                Date beginTime, Date endTime) {
}
