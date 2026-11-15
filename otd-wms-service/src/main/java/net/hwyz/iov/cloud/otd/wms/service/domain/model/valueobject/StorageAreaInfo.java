package net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject;

/**
 * 储区主数据快照（业务动作校验用）
 *
 * @param code          储区代码
 * @param name          储区名称
 * @param warehouseCode 仓库代码
 * @param enabled       是否启用
 * @author hwyz_leo
 */
public record StorageAreaInfo(String code, String name, String warehouseCode, boolean enabled) {
}
