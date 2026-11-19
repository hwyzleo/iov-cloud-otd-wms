package net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject;

import java.util.Date;

/**
 * 待入库任务查询条件
 *
 * @author hwyz_leo
 */
public record InboundTaskCriteria(String orderNum, String sourceOrderNum, String sourceSystem, String warehouseCode,
                                  String state, String vin, Date beginTime, Date endTime) {
}
