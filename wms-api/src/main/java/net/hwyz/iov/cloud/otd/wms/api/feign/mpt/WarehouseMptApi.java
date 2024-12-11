package net.hwyz.iov.cloud.otd.wms.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.otd.wms.api.contract.StorageAreaMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.StorageLocationMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.WarehouseMpt;

import java.util.List;

/**
 * 仓库相关管理后台接口
 *
 * @author hwyz_leo
 */
public interface WarehouseMptApi {

    /**
     * 分页查询仓库信息
     *
     * @param warehouse 仓库信息
     * @return 仓库信息列表
     */
    TableDataInfo list(WarehouseMpt warehouse);

    /**
     * 根据仓库ID获取仓库存储区域
     *
     * @param warehouseId 仓库ID
     * @return 仓库存储区域列表
     */
    List<StorageAreaMpt> listWarehouseStorageArea(Long warehouseId);

    /**
     * 根据仓库ID和存储区域ID获取仓库储区下的存储位置
     *
     * @param warehouseId   仓库ID
     * @param storageAreaId 储区ID
     * @return 存位列表
     */
    List<StorageLocationMpt> listWarehouseStorageAreaStorageLocation(Long warehouseId, Long storageAreaId);

    /**
     * 导出仓库信息
     *
     * @param response  响应
     * @param warehouse 仓库信息
     */
    void export(HttpServletResponse response, WarehouseMpt warehouse);

    /**
     * 根据仓库ID获取仓库信息
     *
     * @param warehouseId 仓库ID
     * @return 仓库信息
     */
    AjaxResult getInfo(Long warehouseId);

    /**
     * 新增仓库信息
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    AjaxResult add(WarehouseMpt warehouse);

    /**
     * 新增仓库储区
     *
     * @param warehouseId 仓库ID
     * @param storageArea 仓库储区
     * @return 结果
     */
    AjaxResult addStorageArea(Long warehouseId, StorageAreaMpt storageArea);

    /**
     * 新增仓库储位
     *
     * @param warehouseId     仓库ID
     * @param storageAreaId   储区ID
     * @param storageLocation 仓库储位
     * @return 结果
     */
    AjaxResult addStorageLocation(Long warehouseId, Long storageAreaId, StorageLocationMpt storageLocation);

    /**
     * 修改保存仓库信息
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    AjaxResult edit(WarehouseMpt warehouse);

    /**
     * 修改保存仓库储区
     *
     * @param warehouseId 仓库ID
     * @param storageArea 仓库储区
     * @return 结果
     */
    AjaxResult editStorageArea(Long warehouseId, StorageAreaMpt storageArea);

    /**
     * 修改保存仓库储位
     *
     * @param warehouseId     仓库ID
     * @param storageAreaId   储区ID
     * @param storageLocation 仓库储位
     * @return 结果
     */
    AjaxResult editStorageLocation(Long warehouseId, Long storageAreaId, StorageLocationMpt storageLocation);

    /**
     * 删除仓库信息
     *
     * @param warehouseIds 仓库ID数组
     * @return 结果
     */
    AjaxResult remove(Long[] warehouseIds);

    /**
     * 删除仓库储区
     *
     * @param warehouseId    仓库ID
     * @param storageAreaIds 仓库储区ID数组
     * @return 结果
     */
    AjaxResult removeStorageArea(Long warehouseId, Long[] storageAreaIds);

    /**
     * 删除仓库储位
     *
     * @param warehouseId        仓库ID
     * @param storageAreaId      储区ID
     * @param storageLocationIds 仓库储位ID数组
     * @return 结果
     */
    AjaxResult removeStorageLocation(Long warehouseId, Long storageAreaId, Long[] storageLocationIds);

}
