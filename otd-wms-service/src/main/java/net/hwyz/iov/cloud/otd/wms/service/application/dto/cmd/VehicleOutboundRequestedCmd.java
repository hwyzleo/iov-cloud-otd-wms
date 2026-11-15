package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

import java.util.List;

/**
 * 通用出库命令
 *
 * @param eventId      事件唯一 ID
 * @param topic        来源 Topic
 * @param payloadDigest 规范化载荷摘要
 * @param requestId    来源系统范围内的命令幂等标识
 * @param sourceSystem 通用请求方标识
 * @param sourceOrderNum 外部来源单号
 * @param warehouseCode 显式目标仓库代码（可空）
 * @param vins         原子预留 VIN 列表
 * @param operator     命令审计操作人
 * @author hwyz_leo
 */
public record VehicleOutboundRequestedCmd(String eventId, String topic, String payloadDigest, String requestId,
                                          String sourceSystem, String sourceOrderNum, String warehouseCode,
                                          List<String> vins, String operator) {
}
