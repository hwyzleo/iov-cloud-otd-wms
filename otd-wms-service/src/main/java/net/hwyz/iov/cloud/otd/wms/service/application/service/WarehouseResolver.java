package net.hwyz.iov.cloud.otd.wms.service.application.service;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.wms.service.application.port.BusinessConfigPort;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.domain.service.StorageHierarchyValidator;
import org.springframework.stereotype.Service;

/**
 * 仓库解析应用服务
 * <p>
 * 显式 {@code warehouseCode} 优先；缺省时使用配置的缺省仓库，禁止由 plantCode 推导仓库。
 *
 * @author hwyz_leo
 */
@Service
@RequiredArgsConstructor
public class WarehouseResolver {

    private final StorageHierarchyValidator storageHierarchyValidator;
    private final BusinessConfigPort businessConfigPort;

    /**
     * 解析并校验仓库
     *
     * @param explicitWarehouseCode 显式仓库代码（可空）
     * @return 已解析且启用的仓库代码
     */
    public String resolve(String explicitWarehouseCode) {
        String warehouseCode = explicitWarehouseCode;
        if (warehouseCode == null || warehouseCode.isBlank()) {
            warehouseCode = businessConfigPort.defaultWarehouseCode();
        }
        if (warehouseCode == null || warehouseCode.isBlank()) {
            throw WmsBusinessException.of(WmsErrorCode.WAREHOUSE_INVALID,
                    "未指定仓库且未配置缺省仓库");
        }
        storageHierarchyValidator.requireEnabledWarehouse(warehouseCode);
        return warehouseCode;
    }
}
