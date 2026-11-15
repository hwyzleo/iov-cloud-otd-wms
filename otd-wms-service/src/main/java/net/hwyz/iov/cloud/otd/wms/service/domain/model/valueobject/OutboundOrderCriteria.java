package net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject;

import java.util.Date;

/**
 * 出库单查询条件（数据库过滤与分页）
 *
 * @author hwyz_leo
 */
public record OutboundOrderCriteria(String orderNum, String sourceSystem, String sourceOrderNum,
                                    String warehouseCode, String state, Date beginTime, Date endTime) {
}
