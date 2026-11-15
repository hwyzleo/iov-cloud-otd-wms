package net.hwyz.iov.cloud.otd.wms.service.adapter.web.controller.mpt;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.audit.annotation.Log;
import net.hwyz.iov.cloud.framework.audit.enums.BusinessType;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.wms.api.contract.StorageAreaMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.StorageLocationMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.WarehouseMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.WarehouseMptApi;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.PageResults;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.WebOperators;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler.MptVoAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.StorageAreaDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.StorageLocationDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.WarehouseDto;
import net.hwyz.iov.cloud.otd.wms.service.application.service.WarehouseAppService;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageAreaPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageLocationPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WarehousePo;
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
public class MptWarehouseController extends BaseController implements WarehouseMptApi {

    private final WarehouseAppService warehouseAppService;

    @RequiresPermissions("completeVehicle:warehouse:info:list")
    @Override
    @GetMapping(value = "/list")
    public ApiResponse<PageResult<WarehouseMpt>> list(WarehouseMpt warehouse) {
        log.info("管理后台用户[{}]分页查询仓库信息", WebOperators.currentOperator());
        startPage();
        List<WarehouseDto> list = warehouseAppService.search(warehouse.getCode(), warehouse.getName(),
                warehouse.getWarehouseLevel(), getBeginTime(warehouse), getEndTime(warehouse));
        return ApiResponse.ok(PageResults.of(list, MptVoAssembler::fromWarehouse));
    }

    @RequiresPermissions("completeVehicle:warehouse:info:list")
    @Override
    @GetMapping(value = "/listByLevel")
    public List<WarehouseMpt> listByLevel(@RequestParam String warehouseLevel) {
        log.info("管理后台用户[{}]根据仓库层级[{}]获取仓库列表", WebOperators.currentOperator(), warehouseLevel);
        return MptVoAssembler.fromWarehouseList(warehouseAppService.search(null, null, warehouseLevel, null, null));
    }

    @RequiresPermissions("completeVehicle:warehouse:info:list")
    @Override
    @GetMapping(value = "/{warehouseId}/storageArea")
    public List<StorageAreaMpt> listWarehouseStorageArea(@PathVariable Long warehouseId) {
        log.info("管理后台用户[{}]根据仓库ID[{}]获取仓库存储区域", WebOperators.currentOperator(), warehouseId);
        return MptVoAssembler.fromStorageAreaList(warehouseAppService.listStorageAreaByWarehouseId(warehouseId));
    }

    @RequiresPermissions("completeVehicle:warehouse:info:list")
    @Override
    @GetMapping(value = "/{warehouseId}/storageArea/{storageAreaId}/storageLocation")
    public List<StorageLocationMpt> listWarehouseStorageAreaStorageLocation(@PathVariable Long warehouseId,
                                                                           @PathVariable Long storageAreaId) {
        log.info("管理后台用户[{}]根据仓库ID[{}]和存储区域ID[{}]获取储位", WebOperators.currentOperator(),
                warehouseId, storageAreaId);
        return MptVoAssembler.fromStorageLocationList(
                warehouseAppService.listStorageLocationByWarehouseIdAndStorageAreaId(warehouseId, storageAreaId));
    }

    @Log(title = "仓库管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:info:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, WarehouseMpt warehouse) {
        log.info("管理后台用户[{}]导出仓库信息", WebOperators.currentOperator());
    }

    @RequiresPermissions("completeVehicle:warehouse:info:query")
    @Override
    @GetMapping(value = "/{warehouseId}")
    public ApiResponse<WarehouseMpt> getInfo(@PathVariable Long warehouseId) {
        log.info("管理后台用户[{}]根据仓库ID[{}]获取仓库信息", WebOperators.currentOperator(), warehouseId);
        return ApiResponse.ok(MptVoAssembler.fromWarehouse(warehouseAppService.getWarehouseById(warehouseId)));
    }

    @Log(title = "仓库管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:info:add")
    @Override
    @PostMapping
    public ApiResponse<Void> add(@Validated @RequestBody WarehouseMpt warehouse) {
        log.info("管理后台用户[{}]新增仓库信息[{}]", WebOperators.currentOperator(), warehouse.getCode());
        if (!warehouseAppService.checkCodeUnique(warehouse.getId(), warehouse.getCode())) {
            return ApiResponse.fail("新增仓库'" + warehouse.getCode() + "'失败，仓库代码已存在");
        }
        WarehousePo po = MptVoAssembler.toPo(warehouse);
        po.setCreateBy(WebOperators.currentOperator());
        return warehouseAppService.createWarehouse(po) > 0 ? ApiResponse.ok() : ApiResponse.fail("新增仓库失败");
    }

    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PostMapping("/{warehouseId}/storageArea")
    public ApiResponse<Void> addStorageArea(@PathVariable Long warehouseId,
                                            @Validated @RequestBody StorageAreaMpt storageArea) {
        log.info("管理后台用户[{}]新增仓库[{}]储区[{}]", WebOperators.currentOperator(), warehouseId,
                storageArea.getCode());
        if (!warehouseAppService.checkStorageAreaCodeUnique(storageArea.getId(), MptVoAssembler.toPo(storageArea))) {
            return ApiResponse.fail("储区代码'" + storageArea.getCode() + "'在本仓库内已存在");
        }
        StorageAreaPo po = MptVoAssembler.toPo(storageArea);
        po.setCreateBy(WebOperators.currentOperator());
        return warehouseAppService.createStorageArea(po) > 0 ? ApiResponse.ok() : ApiResponse.fail("新增储区失败");
    }

    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PostMapping("/{warehouseId}/storageArea/{storageAreaId}/storageLocation")
    public ApiResponse<Void> addStorageLocation(@PathVariable Long warehouseId, @PathVariable Long storageAreaId,
                                                @Validated @RequestBody StorageLocationMpt storageLocation) {
        log.info("管理后台用户[{}]新增仓库[{}]储区[{}]储位[{}]", WebOperators.currentOperator(), warehouseId,
                storageAreaId, storageLocation.getCode());
        if (!warehouseAppService.checkStorageLocationCodeUnique(storageLocation.getId(),
                MptVoAssembler.toPo(storageLocation))) {
            return ApiResponse.fail("储位代码'" + storageLocation.getCode() + "'在本储区内已存在");
        }
        StorageLocationPo po = MptVoAssembler.toPo(storageLocation);
        po.setCreateBy(WebOperators.currentOperator());
        return warehouseAppService.createStorageLocation(po) > 0 ? ApiResponse.ok() : ApiResponse.fail("新增储位失败");
    }

    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PutMapping
    public ApiResponse<Void> edit(@Validated @RequestBody WarehouseMpt warehouse) {
        log.info("管理后台用户[{}]修改保存仓库信息[{}]", WebOperators.currentOperator(), warehouse.getCode());
        if (!warehouseAppService.checkCodeUnique(warehouse.getId(), warehouse.getCode())) {
            return ApiResponse.fail("修改保存仓库'" + warehouse.getCode() + "'失败，仓库代码已存在");
        }
        WarehousePo po = MptVoAssembler.toPo(warehouse);
        po.setModifyBy(WebOperators.currentOperator());
        return warehouseAppService.modifyWarehouse(po) > 0 ? ApiResponse.ok() : ApiResponse.fail("修改仓库失败");
    }

    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PutMapping("/{warehouseId}/storageArea")
    public ApiResponse<Void> editStorageArea(@PathVariable Long warehouseId,
                                             @Validated @RequestBody StorageAreaMpt storageArea) {
        log.info("管理后台用户[{}]修改保存仓库[{}]储区[{}]", WebOperators.currentOperator(), warehouseId,
                storageArea.getCode());
        if (!warehouseAppService.checkStorageAreaCodeUnique(storageArea.getId(), MptVoAssembler.toPo(storageArea))) {
            return ApiResponse.fail("储区代码'" + storageArea.getCode() + "'在本仓库内已存在");
        }
        StorageAreaPo po = MptVoAssembler.toPo(storageArea);
        po.setModifyBy(WebOperators.currentOperator());
        return warehouseAppService.modifyStorageArea(po) > 0 ? ApiResponse.ok() : ApiResponse.fail("修改储区失败");
    }

    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @PutMapping("/{warehouseId}/storageArea/{storageAreaId}/storageLocation")
    public ApiResponse<Void> editStorageLocation(@PathVariable Long warehouseId, @PathVariable Long storageAreaId,
                                                 @Validated @RequestBody StorageLocationMpt storageLocation) {
        log.info("管理后台用户[{}]修改保存仓库[{}]储区[{}]储位[{}]", WebOperators.currentOperator(), warehouseId,
                storageAreaId, storageLocation.getCode());
        if (!warehouseAppService.checkStorageLocationCodeUnique(storageLocation.getId(),
                MptVoAssembler.toPo(storageLocation))) {
            return ApiResponse.fail("储位代码'" + storageLocation.getCode() + "'在本储区内已存在");
        }
        StorageLocationPo po = MptVoAssembler.toPo(storageLocation);
        po.setModifyBy(WebOperators.currentOperator());
        return warehouseAppService.modifyStorageLocation(po) > 0 ? ApiResponse.ok() : ApiResponse.fail("修改储位失败");
    }

    @Log(title = "仓库管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:warehouse:info:remove")
    @Override
    @DeleteMapping("/{warehouseIds}")
    public ApiResponse<Void> remove(@PathVariable Long[] warehouseIds) {
        log.info("管理后台用户[{}]删除仓库信息[{}]", WebOperators.currentOperator(), warehouseIds);
        return warehouseAppService.deleteWarehouseByIds(warehouseIds) > 0
                ? ApiResponse.ok() : ApiResponse.fail("删除仓库失败");
    }

    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @DeleteMapping("/{warehouseId}/storageArea/{storageAreaIds}")
    public ApiResponse<Void> removeStorageArea(@PathVariable Long warehouseId, @PathVariable Long[] storageAreaIds) {
        log.info("管理后台用户[{}]删除仓库[{}]储区[{}]", WebOperators.currentOperator(), warehouseId,
                storageAreaIds);
        return warehouseAppService.deleteStorageAreaByIds(warehouseId, storageAreaIds) > 0
                ? ApiResponse.ok() : ApiResponse.fail("删除储区失败");
    }

    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:info:edit")
    @Override
    @DeleteMapping("/{warehouseId}/storageArea/{storageAreaId}/storageLocation/{storageLocationIds}")
    public ApiResponse<Void> removeStorageLocation(@PathVariable Long warehouseId, @PathVariable Long storageAreaId,
                                                   @PathVariable Long[] storageLocationIds) {
        log.info("管理后台用户[{}]删除仓库[{}]储区[{}]储位[{}]", WebOperators.currentOperator(), warehouseId,
                storageAreaId, storageLocationIds);
        return warehouseAppService.deleteStorageLocationByIds(warehouseId, storageAreaId, storageLocationIds) > 0
                ? ApiResponse.ok() : ApiResponse.fail("删除储位失败");
    }
}
