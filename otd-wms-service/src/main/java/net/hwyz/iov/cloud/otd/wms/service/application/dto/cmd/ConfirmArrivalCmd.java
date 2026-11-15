package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 确认到达命令
 *
 * @param orderNum        预入库单号
 * @param requestId       请求幂等标识
 * @param expectedVersion 预期行版本
 * @param operator        可信操作人
 * @author hwyz_leo
 */
public record ConfirmArrivalCmd(String orderNum, String requestId, Integer expectedVersion, String operator) {
}
