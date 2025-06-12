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
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.InventoryMptApi;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InventoryAppService;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.InventoryMptAssembler;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryPo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/inventory")
public class InventoryMptController extends BaseController implements InventoryMptApi {

    private final InventoryAppService inventoryAppService;

    /**
     * 分页查询库存
     *
     * @param inventory 库存
     * @return 库存列表
     */
    @RequiresPermissions("completeVehicle:warehouse:inventory:list")
    @Override
    @GetMapping(value = "/list")
    public TableDataInfo list(InventoryMpt inventory) {
        logger.info("管理后台用户[{}]分页查询库存", SecurityUtils.getUsername());
        startPage();
        List<InventoryPo> inventoryPoList = inventoryAppService.search(inventory.getVin(), inventory.getWarehouseLevel(),
                inventory.getWarehouseCode(), getBeginTime(inventory), getEndTime(inventory));
        List<InventoryMpt> inventoryMptList = InventoryMptAssembler.INSTANCE.fromPoList(inventoryPoList);
        return getDataTable(inventoryPoList, inventoryMptList);
    }

    /**
     * 导出库存
     *
     * @param response  响应
     * @param inventory 库存
     */
    @Log(title = "库存管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:inventory:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, InventoryMpt inventory) {
        logger.info("管理后台用户[{}]导出库存", SecurityUtils.getUsername());
    }

    /**
     * 根据库存ID获取库存
     *
     * @param inventoryId 库存ID
     * @return 库存
     */
    @RequiresPermissions("completeVehicle:warehouse:inventory:query")
    @Override
    @GetMapping(value = "/{inventoryId}")
    public AjaxResult getInfo(@PathVariable Long inventoryId) {
        logger.info("管理后台用户[{}]根据预库存ID[{}]获取库存", SecurityUtils.getUsername(), inventoryId);
        InventoryPo inventoryPo = inventoryAppService.getInventoryById(inventoryId);
        return success(InventoryMptAssembler.INSTANCE.fromPo(inventoryPo));
    }

}
