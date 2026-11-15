package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 消费 VMD 车辆生产事实命令
 *
 * @param eventId          事件唯一 ID
 * @param topic            来源 Topic
 * @param batchNum         来源批次号
 * @param vin              车架号
 * @param configurationCode 生产配置代码（映射为 buildConfigCode）
 * @param warehouseCode    显式仓库代码（可空）
 * @param payloadDigest    规范化载荷摘要
 * @author hwyz_leo
 */
public record ConsumeVehicleProducedCmd(String eventId, String topic, String batchNum, String vin,
                                         String configurationCode, String warehouseCode, String payloadDigest) {
}
