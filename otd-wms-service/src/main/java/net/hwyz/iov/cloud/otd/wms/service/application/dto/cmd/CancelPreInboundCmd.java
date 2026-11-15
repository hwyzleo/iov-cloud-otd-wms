package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 取消预入库命令
 *
 * @param orderNum        预入库单号
 * @param requestId       请求幂等标识
 * @param reason          取消原因
 * @param expectedVersion 预期行版本
 * @param operator        可信操作人
 * @author hwyz_leo
 */
public record CancelPreInboundCmd(String orderNum, String requestId, String reason,
                                  Integer expectedVersion, String operator) {
}
