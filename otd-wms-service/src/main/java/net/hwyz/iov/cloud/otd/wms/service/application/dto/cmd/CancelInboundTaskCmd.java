package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 待入库任务取消命令
 *
 * @param orderNum        任务号
 * @param requestId       请求幂等标识
 * @param reason          取消原因
 * @param expectedVersion 预期任务版本
 * @param operator        可信操作人
 * @author hwyz_leo
 */
public record CancelInboundTaskCmd(String orderNum, String requestId, String reason, Integer expectedVersion,
                                   String operator) {
}
