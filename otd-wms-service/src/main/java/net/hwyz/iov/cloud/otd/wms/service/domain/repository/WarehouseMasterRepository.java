package net.hwyz.iov.cloud.otd.wms.service.domain.repository;

import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.StorageAreaInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.StorageLocationInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.WarehouseInfo;

import java.util.Optional;

/**
 * 仓储主数据仓储（领域定义的持久化端口）
 *
 * @author hwyz_leo
 */
public interface WarehouseMasterRepository {

    /**
     * 查询仓库
     *
     * @param code 仓库代码
     * @return 仓库快照
     */
    Optional<WarehouseInfo> findWarehouse(String code);

    /**
     * 查询储区
     *
     * @param warehouseCode 仓库代码
     * @param areaCode      储区代码
     * @return 储区快照
     */
    Optional<StorageAreaInfo> findArea(String warehouseCode, String areaCode);

    /**
     * 查询储位
     *
     * @param warehouseCode 仓库代码
     * @param areaCode      储区代码
     * @param locationCode  储位代码
     * @return 储位快照
     */
    Optional<StorageLocationInfo> findLocation(String warehouseCode, String areaCode, String locationCode);

    /**
     * 仓库是否被库存或已完成事实引用
     *
     * @param warehouseCode 仓库代码
     * @return 是否被引用
     */
    boolean isWarehouseReferenced(String warehouseCode);

    /**
     * 储区是否被库存或已完成事实引用
     *
     * @param warehouseCode 仓库代码
     * @param areaCode      储区代码
     * @return 是否被引用
     */
    boolean isAreaReferenced(String warehouseCode, String areaCode);

    /**
     * 储位是否被库存或已完成事实引用
     *
     * @param warehouseCode 仓库代码
     * @param locationCode  储位代码
     * @return 是否被引用
     */
    boolean isLocationReferenced(String warehouseCode, String locationCode);
}
