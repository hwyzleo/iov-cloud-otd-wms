package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

import java.util.List;

/**
 * 到货请求命令
 *
 * @param eventId             事件 ID
 * @param topic               来源 Topic
 * @param payloadDigest       载荷摘要
 * @param requestId           来源幂等标识
 * @param sourceSystem        来源系统
 * @param sourceOrderNum      来源单号
 * @param warehouseCode       目标交付库
 * @param vins                VIN 列表
 * @param estimatedArrivalTime 预计到达时间（ISO-8601，可空）
 * @param operator            可信操作人
 * @author hwyz_leo
 */
public record VehicleInboundRequestedCmd(String eventId, String topic, String payloadDigest, String requestId,
                                         String sourceSystem, String sourceOrderNum, String warehouseCode,
                                         List<String> vins, String estimatedArrivalTime, String operator) {
}
