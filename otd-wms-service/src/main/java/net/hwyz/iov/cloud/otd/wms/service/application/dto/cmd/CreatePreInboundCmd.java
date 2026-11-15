package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 创建预入库单命令（存量同步接口，非 VMD 接入路径）
 *
 * @param vin              车架号
 * @param buildConfigCode  生产配置代码
 * @param warehouseCode    仓库代码（可空，空则取缺省仓库）
 * @param warehouseLevel   仓库体系层级（可空）
 * @param estimatedArrivalTime 预计到达时间
 * @param estimatedInboundTime 预计入库时间
 * @param operator         操作人
 * @author hwyz_leo
 */
public record CreatePreInboundCmd(String vin, String buildConfigCode, String warehouseCode, String warehouseLevel,
                                  java.util.Date estimatedArrivalTime, java.util.Date estimatedInboundTime,
                                  String operator) {
}
