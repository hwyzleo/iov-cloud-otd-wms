package net.hwyz.iov.cloud.otd.wms.service.domain.service;

import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.StorageAreaInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.StorageLocationInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.WarehouseInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.WarehouseMasterRepository;

/**
 * 仓储主数据层级校验领域服务
 * <p>
 * 校验仓库、储区、储位存在、启用且层级归属一致；不承担并发唯一性保证。
 *
 * @author hwyz_leo
 */
public class StorageHierarchyValidator {

    private final WarehouseMasterRepository warehouseMasterRepository;

    public StorageHierarchyValidator(WarehouseMasterRepository warehouseMasterRepository) {
        this.warehouseMasterRepository = warehouseMasterRepository;
    }

    /**
     * 要求仓库存在且启用
     *
     * @param warehouseCode 仓库代码
     * @return 仓库快照
     */
    public WarehouseInfo requireEnabledWarehouse(String warehouseCode) {
        WarehouseInfo warehouse = warehouseMasterRepository.findWarehouse(warehouseCode)
                .orElseThrow(() -> WmsBusinessException.of(WmsErrorCode.WAREHOUSE_INVALID,
                        "仓库[" + warehouseCode + "]不存在"));
        if (!warehouse.enabled()) {
            throw WmsBusinessException.of(WmsErrorCode.WAREHOUSE_INVALID,
                    "仓库[" + warehouseCode + "]已停用");
        }
        return warehouse;
    }

    /**
     * 要求储位存在、启用且层级归属一致
     *
     * @param warehouseCode 仓库代码
     * @param areaCode      储区代码
     * @param locationCode  储位代码
     * @return 储位快照
     */
    public StorageLocationInfo requireEnabledLocation(String warehouseCode, String areaCode, String locationCode) {
        StorageAreaInfo area = warehouseMasterRepository.findArea(warehouseCode, areaCode)
                .orElseThrow(() -> WmsBusinessException.of(WmsErrorCode.STORAGE_LOCATION_INVALID,
                        "仓库[" + warehouseCode + "]不存在储区[" + areaCode + "]"));
        if (!area.enabled()) {
            throw WmsBusinessException.of(WmsErrorCode.STORAGE_LOCATION_INVALID,
                    "储区[" + areaCode + "]已停用");
        }
        StorageLocationInfo location = warehouseMasterRepository.findLocation(warehouseCode, areaCode, locationCode)
                .orElseThrow(() -> WmsBusinessException.of(WmsErrorCode.STORAGE_LOCATION_INVALID,
                        "储区[" + areaCode + "]不存在储位[" + locationCode + "]"));
        if (!location.enabled()) {
            throw WmsBusinessException.of(WmsErrorCode.STORAGE_LOCATION_INVALID,
                    "储位[" + locationCode + "]已停用");
        }
        return location;
    }
}
