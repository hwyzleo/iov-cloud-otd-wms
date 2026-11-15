package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 取消出库单命令
 *
 * @param orderNum        出库单号
 * @param requestId       请求幂等标识
 * @param reason          取消原因
 * @param expectedVersion 预期订单行版本
 * @param operator        可信操作人
 * @author hwyz_leo
 */
public record CancelOutboundOrderCmd(String orderNum, String requestId, String reason,
                                     Integer expectedVersion, String operator) {
}
