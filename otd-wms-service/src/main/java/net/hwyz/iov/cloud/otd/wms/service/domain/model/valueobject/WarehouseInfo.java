package net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject;

/**
 * 仓库主数据快照（业务动作校验用）
 *
 * @param code  仓库代码
 * @param name  仓库名称
 * @param level 仓库体系层级
 * @param enabled 是否启用
 * @author hwyz_leo
 */
public record WarehouseInfo(String code, String name, String level, boolean enabled) {
}
