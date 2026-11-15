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
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.InventoryMptApi;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.PageResults;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.WebOperators;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler.MptVoAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.WmsResultAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InventoryAppService;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InventoryCriteria;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存相关管理接口实现类
 * <p>
 * 当前库存仅由业务动作维护，不暴露通用新增／修改／删除。
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/inventory")
public class MptInventoryController extends BaseController implements InventoryMptApi {

    private final InventoryAppService inventoryAppService;

    @RequiresPermissions("completeVehicle:warehouse:inventory:list")
    @Override
    @GetMapping(value = "/list")
    public ApiResponse<PageResult<InventoryMpt>> list(InventoryMpt inventory) {
        logger.info("管理后台用户[{}]分页查询库存", WebOperators.currentOperator());
        startPage();
        List<VehicleInventory> list = inventoryAppService.search(new InventoryCriteria(
                inventory.getVin(), inventory.getWarehouseLevel(), inventory.getWarehouseCode(),
                inventory.getState(), inventory.getStorageAreaCode(), inventory.getStorageLocationCode(),
                getBeginTime(inventory), getEndTime(inventory)));
        return ApiResponse.ok(PageResults.of(WmsResultAssembler.fromInventoryList(list),
                MptVoAssembler::fromInventory));
    }

    @Log(title = "库存管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:inventory:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, InventoryMpt inventory) {
        logger.info("管理后台用户[{}]导出库存", WebOperators.currentOperator());
    }

    @RequiresPermissions("completeVehicle:warehouse:inventory:query")
    @Override
    @GetMapping(value = "/{inventoryId}")
    public ApiResponse<InventoryMpt> getInfo(@PathVariable Long inventoryId) {
        logger.info("管理后台用户[{}]根据库存ID[{}]获取库存", WebOperators.currentOperator(), inventoryId);
        VehicleInventory inventory = inventoryAppService.getById(inventoryId);
        return ApiResponse.ok(MptVoAssembler.fromInventory(WmsResultAssembler.fromInventory(inventory)));
    }
}
