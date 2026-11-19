package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 待入库任务入库确认命令
 *
 * @param orderNum            任务号
 * @param requestId           请求幂等标识
 * @param storageAreaCode     储区代码
 * @param storageLocationCode 储位代码
 * @param expectedVersion     预期任务版本
 * @param operator            可信操作人
 * @author hwyz_leo
 */
public record ConfirmInboundTaskCmd(String orderNum, String requestId, String storageAreaCode,
                                    String storageLocationCode, Integer expectedVersion, String operator) {
}
