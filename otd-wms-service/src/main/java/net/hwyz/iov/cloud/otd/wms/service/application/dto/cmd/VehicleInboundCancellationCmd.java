package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 到货请求取消命令
 *
 * @param eventId        事件 ID
 * @param topic          来源 Topic
 * @param payloadDigest  载荷摘要
 * @param requestId      来源幂等标识
 * @param sourceSystem   来源系统
 * @param sourceOrderNum 来源单号
 * @param reason         取消原因
 * @param operator       可信操作人
 * @author hwyz_leo
 */
public record VehicleInboundCancellationCmd(String eventId, String topic, String payloadDigest, String requestId,
                                            String sourceSystem, String sourceOrderNum, String reason,
                                            String operator) {
}
