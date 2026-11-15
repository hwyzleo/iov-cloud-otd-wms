package net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject;

import java.util.Date;

/**
 * 预入库单查询条件（数据库过滤与分页）
 *
 * @author hwyz_leo
 */
public record PreInboundOrderCriteria(String orderNum, String vin, String warehouseLevel, String warehouseCode,
                                      String state, Date beginTime, Date endTime) {
}
