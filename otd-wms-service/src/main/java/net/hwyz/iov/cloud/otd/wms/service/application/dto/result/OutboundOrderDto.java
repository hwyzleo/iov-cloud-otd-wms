package net.hwyz.iov.cloud.otd.wms.service.application.dto.result;

import lombok.Builder;

import java.util.Date;
import java.util.List;

/**
 * 出库单查询结果
 *
 * @author hwyz_leo
 */
@Builder
public record OutboundOrderDto(Long id, String orderNum, String requestId, String sourceSystem,
                               String sourceOrderNum, String warehouseCode, String state,
                               Long aggregateVersion, Integer rowVersion, Date createTime, Date modifyTime,
                               List<OutboundOrderDetailDto> details) {
}
