package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 通用出库取消命令
 *
 * @param eventId       事件唯一 ID
 * @param topic         来源 Topic
 * @param payloadDigest 规范化载荷摘要
 * @param requestId     来源系统范围内的命令幂等标识
 * @param sourceSystem  通用请求方标识
 * @param sourceOrderNum 外部来源单号
 * @param reason        取消原因
 * @param operator      命令审计操作人
 * @author hwyz_leo
 */
public record VehicleOutboundCancellationCmd(String eventId, String topic, String payloadDigest, String requestId,
                                             String sourceSystem, String sourceOrderNum, String reason,
                                             String operator) {
}
