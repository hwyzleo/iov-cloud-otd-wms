package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 按 VIN 出库命令
 *
 * @param orderNum        出库单号
 * @param vin             车架号
 * @param requestId       请求幂等标识
 * @param expectedVersion 预期明细行版本
 * @param operator        可信操作人
 * @author hwyz_leo
 */
public record OutboundVinCmd(String orderNum, String vin, String requestId, Integer expectedVersion,
                             String operator) {
}
