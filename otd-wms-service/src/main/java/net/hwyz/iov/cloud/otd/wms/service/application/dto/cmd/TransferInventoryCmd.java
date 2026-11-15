package net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd;

/**
 * 同仓移储命令
 *
 * @param requestId        请求幂等标识
 * @param vin              车架号
 * @param warehouseCode    仓库代码
 * @param newStorageAreaCode     目标储区代码
 * @param newStorageLocationCode 目标储位代码
 * @param expectedVersion  预期库存行版本
 * @param operator         可信操作人
 * @author hwyz_leo
 */
public record TransferInventoryCmd(String requestId, String vin, String warehouseCode,
                                   String newStorageAreaCode, String newStorageLocationCode,
                                   Integer expectedVersion, String operator) {
}
