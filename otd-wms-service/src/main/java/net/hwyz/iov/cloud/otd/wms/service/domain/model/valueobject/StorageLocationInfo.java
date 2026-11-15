package net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject;

/**
 * 储位主数据快照（业务动作校验用）
 *
 * @param code            储位代码
 * @param name            储位名称
 * @param warehouseCode   仓库代码
 * @param storageAreaCode 储区代码
 * @param enabled         是否启用
 * @author hwyz_leo
 */
public record StorageLocationInfo(String code, String name, String warehouseCode, String storageAreaCode,
                                  boolean enabled) {
}
