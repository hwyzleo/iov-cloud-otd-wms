package net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject;

import java.util.Date;

/**
 * 入库单查询条件（数据库过滤与分页）
 *
 * @author hwyz_leo
 */
public record InboundOrderCriteria(String orderNum, String preOrderNum, String vin, String warehouseLevel,
                                   String warehouseCode, Date beginTime, Date endTime) {
}
