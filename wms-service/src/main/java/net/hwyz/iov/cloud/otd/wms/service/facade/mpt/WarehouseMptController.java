package net.hwyz.iov.cloud.otd.wms.service.facade.mpt;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.audit.annotation.Log;
import net.hwyz.iov.cloud.framework.audit.enums.BusinessType;
import net.hwyz.iov.cloud.framework.common.web.controller.BaseController;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.framework.security.util.SecurityUtils;
import net.hwyz.iov.cloud.otd.wms.api.contract.StorageAreaMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.StorageLocationMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.WarehouseMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.WarehouseMptApi;
import net.hwyz.iov.cloud.otd.wms.service.application.service.WarehouseAppService;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.StorageAreaMptAssembler;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.StorageLocationMptAssembler;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.WarehouseMptAssembler;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.StorageAreaPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.StorageLocationPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.WarehousePo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/warehouse")
public class WarehouseMptController extends BaseController implements WarehouseMptApi {

    private final WarehouseAppService warehouseAppService;

    /**
     * 分页查询仓库信息
     *
     * @param warehouse 仓库信息
     * @return 仓库信息列表
     */
    @RequiresPermissions("completeVehicle:warehouse:info:list")
    @Override
    @GetMapping(value = "/list")
    public TableDataInfo list(WarehouseMpt warehouse) {
        logger.info("管理后台用户[{}]分页查询仓库信息", SecurityUtils.getUsername());
        startPage();
        List<WarehousePo> warehousePoList = warehouseAppService.search(warehouse.getCode(), warehouse.getName(),
                warehouse.getWarehouseLevel(), getBeginTime(warehouse), getEndTime(warehouse));
        List<WarehouseMpt> warehouseMptList = WarehouseMptAssembler.INSTANCE.fromPoList(warehousePoList);
        return getDataTable(warehousePoList, warehouseMptList);
    }

    /**
     * 根据仓库ID获取仓库存储区域
     *
     * @param warehouseId 仓库ID
     * @return 仓库存储区域列表
     */
    @RequiresPermissions("completeVehicle:warehouse:info:list")
    @Override
    @GetMapping(value = "/{warehouseId}/storageArea")
    public List<StorageAreaMpt> listWarehouseStorageArea(@PathVariable Long warehouseId) {
        logger.info("管理后台用户[{}]根据仓库ID[{}]获取仓库存储区域", SecurityUtils.getUsername(), warehouseId);
        List<StorageAreaPo> storageAreaPoList = warehouseAppService.listStorageAreaByWarehouseId(warehouseId);
        return StorageAreaMptAssembler.INSTANCE.fromPoList(storageAreaPoList);
    }

    /**
     * 根据仓库ID和存储区域ID获取仓库储区下的存储位置
     *
     * @param warehouseId   仓库ID
     * @param storageAreaId 储区ID
     * @return 存位列表
     */
    @RequiresPermissions("completeVehicle:warehouse:info:list")
    @Override
    @GetMapping(value = "/{warehouseId}/storageArea/{storageAreaId}/storageLocation")
    public List<StorageLocationMpt> listWarehouseStorageAreaStorageLocation(@PathVariable Long warehouseId, @PathVariable Long storageAreaId) {
        logger.info("管理后台用户[{}]根据仓库ID[{}]和存储区域ID[{}]获取仓库储区下的存储位置", SecurityUtils.getUsername(), warehouseId, storageAreaId);
        List<StorageLocationPo> storageLocationPoList = warehouseAppService.listStorageLocationByWarehouseIdAndStorageAreaId(warehouseId, storageAreaId);
        return StorageLocationMptAssembler.INSTANCE.fromPoList(storageLocationPoList);
    }

    /**
     * 导出仓库信息
     *
     * @param response  响应
     * @param saleModel 仓库信息
     */
    @Log(title = "仓库管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:info:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, WarehouseMpt saleModel) {
        logger.info("管理后台用户[{}]导出仓库信息", SecurityUtils.getUsername());
    }

    /**
     * 根据仓库ID获取仓库信息
     *
     * @param warehouseId 仓库ID
     * @return 仓库信息
     */
    @RequiresPermissions("completeVehicle:warehouse:info:query")
    @Override
    @GetMapping(value = "/{warehouseId}")
    public AjaxResult getInfo(@PathVariable Long warehouseId) {
        logger.info("管理后台用户[{}]根据仓库ID[{}]获取仓库信息", SecurityUtils.getUsername(), warehouseId);
        WarehousePo warehousePo = warehouseAppService.getWarehouseById(warehouseId);
        return success(WarehouseMptAssembler.INSTANCE.fromPo(warehousePo));
    }

    /**
     * 新增仓库信息
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    @Log(title = "仓库管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:info:add")
    @Override
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WarehouseMpt warehouse) {
        logger.info("管理后台用户[{}]新增仓库信息[{}]", SecurityUtils.getUsername(), warehouse.getCode());
        if (!warehouseAppService.checkCodeUnique(warehouse.getId(), warehouse.getCode())) {
            return error("新增仓库'" + warehouse.getCode() + "'失败，仓库代码已存在");
        }
        WarehousePo warehousePo = WarehouseMptAssembler.INSTANCE.toPo(warehouse);
        warehousePo.setCreateBy(SecurityUtils.getUserId().toString());
        return toAjax(warehouseAppService.createWarehouse(warehousePo));
    }

    /**
     * 新增仓库储区
     *
     * @param storageArea 仓库储区
     * @return 结果
     */
    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PostMapping("/{warehouseId}/storageArea")
    public AjaxResult addStorageArea(@PathVariable Long warehouseId, @Validated @RequestBody StorageAreaMpt storageArea) {
        logger.info("管理后台用户[{}]新增仓库[{}]储区[{}]", SecurityUtils.getUsername(), warehouseId, storageArea.getCode());
        StorageAreaPo storageAreaPo = StorageAreaMptAssembler.INSTANCE.toPo(storageArea);
        storageAreaPo.setCreateBy(SecurityUtils.getUserId().toString());
        return toAjax(warehouseAppService.createStorageArea(storageAreaPo));
    }

    /**
     * 新增仓库储位
     *
     * @param warehouseId     仓库ID
     * @param storageAreaId   储区ID
     * @param storageLocation 仓库储位
     * @return 结果
     */
    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PostMapping("/{warehouseId}/storageArea/{storageAreaId}/storageLocation")
    public AjaxResult addStorageLocation(@PathVariable Long warehouseId, @PathVariable Long storageAreaId,
                                         @Validated @RequestBody StorageLocationMpt storageLocation) {
        logger.info("管理后台用户[{}]新增仓库[{}]储区[{}]储位[{}]", SecurityUtils.getUsername(), warehouseId, storageAreaId,
                storageLocation.getCode());
        StorageLocationPo storageLocationPo = StorageLocationMptAssembler.INSTANCE.toPo(storageLocation);
        storageLocationPo.setCreateBy(SecurityUtils.getUserId().toString());
        return toAjax(warehouseAppService.createStorageLocation(storageLocationPo));
    }

    /**
     * 修改保存仓库信息
     *
     * @param warehouse 仓库信息
     * @return 结果
     */
    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WarehouseMpt warehouse) {
        logger.info("管理后台用户[{}]修改保存仓库信息[{}]", SecurityUtils.getUsername(), warehouse.getCode());
        if (!warehouseAppService.checkCodeUnique(warehouse.getId(), warehouse.getCode())) {
            return error("修改保存仓库'" + warehouse.getCode() + "'失败，仓库代码已存在");
        }
        WarehousePo warehousePo = WarehouseMptAssembler.INSTANCE.toPo(warehouse);
        warehousePo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(warehouseAppService.modifyWarehouse(warehousePo));
    }

    /**
     * 修改保存仓库储区
     *
     * @param storageArea 仓库储区
     * @return 结果
     */
    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PutMapping("/{warehouseId}/storageArea")
    public AjaxResult editStorageArea(@PathVariable Long warehouseId, @Validated @RequestBody StorageAreaMpt storageArea) {
        logger.info("管理后台用户[{}]修改保存仓库[{}]储区[{}]", SecurityUtils.getUsername(), warehouseId, storageArea.getCode());
        StorageAreaPo storageAreaPo = StorageAreaMptAssembler.INSTANCE.toPo(storageArea);
        storageAreaPo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(warehouseAppService.modifyStorageArea(storageAreaPo));
    }

    /**
     * 修改保存仓库储位
     *
     * @param warehouseId     仓库ID
     * @param storageAreaId   储区ID
     * @param storageLocation 仓库储位
     * @return 结果
     */
    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PutMapping("/{warehouseId}/storageArea/{storageAreaId}/storageLocation")
    public AjaxResult editStorageLocation(@PathVariable Long warehouseId, @PathVariable Long storageAreaId,
                                          @Validated @RequestBody StorageLocationMpt storageLocation) {
        logger.info("管理后台用户[{}]修改保存仓库[{}]储区[{}]储位[{}]", SecurityUtils.getUsername(), warehouseId, storageAreaId, storageLocation.getCode());
        StorageLocationPo storageLocationPo = StorageLocationMptAssembler.INSTANCE.toPo(storageLocation);
        storageLocationPo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(warehouseAppService.modifyStorageLocation(storageLocationPo));
    }

    /**
     * 删除仓库信息
     *
     * @param warehouseIds 仓库ID数组
     * @return 结果
     */
    @Log(title = "仓库管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:warehouse:info:remove")
    @Override
    @DeleteMapping("/{warehouseIds}")
    public AjaxResult remove(@PathVariable Long[] warehouseIds) {
        logger.info("管理后台用户[{}]删除仓库信息[{}]", SecurityUtils.getUsername(), warehouseIds);
        return toAjax(warehouseAppService.deleteWarehouseByIds(warehouseIds));
    }

    /**
     * 删除仓库储区
     *
     * @param warehouseId    仓库ID
     * @param storageAreaIds 仓库储区ID数组
     * @return 结果
     */
    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @DeleteMapping("/{warehouseId}/storageArea/{storageAreaIds}")
    public AjaxResult removeStorageArea(@PathVariable Long warehouseId, @PathVariable Long[] storageAreaIds) {
        logger.info("管理后台用户[{}]删除仓库[{}]储区[{}]", SecurityUtils.getUsername(), warehouseId, storageAreaIds);
        return toAjax(warehouseAppService.deleteStorageAreaByIds(warehouseId, storageAreaIds));
    }

    /**
     * 删除仓库储位
     *
     * @param warehouseId        仓库ID
     * @param storageAreaId      储区ID
     * @param storageLocationIds 仓库储位ID数组
     * @return 结果
     */
    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @DeleteMapping("/{warehouseId}/storageArea/{storageAreaId}/storageLocation/{storageLocationIds}")
    public AjaxResult removeStorageLocation(@PathVariable Long warehouseId, @PathVariable Long storageAreaId,
                                            @PathVariable Long[] storageLocationIds) {
        logger.info("管理后台用户[{}]删除仓库[{}]储区[{}]储位[{}]", SecurityUtils.getUsername(), warehouseId, storageAreaId, storageLocationIds);
        return toAjax(warehouseAppService.deleteStorageLocationByIds(warehouseId, storageAreaId, storageLocationIds));
    }
}
