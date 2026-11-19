package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 待入库任务到货确认命令
 *
 * @param orderNum        任务号
 * @param requestId       请求幂等标识
 * @param expectedVersion 预期任务版本
 * @param operator        可信操作人
 * @author hwyz_leo
 */
public record ArrivalInboundTaskCmd(String orderNum, String requestId, Integer expectedVersion, String operator) {
}
