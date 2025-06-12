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
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryTransferMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.InventoryTransferMptApi;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InventoryTransferAppService;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.InventoryTransferMptAssembler;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryTransferPo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 移库相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/inventoryTransfer")
public class InventoryTransferMptController extends BaseController implements InventoryTransferMptApi {

    private final InventoryTransferAppService inventoryTransferAppService;

    /**
     * 分页查询移库信息
     *
     * @param inventoryTransfer 移库信息
     * @return 移库信息列表
     */
    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:list")
    @Override
    @GetMapping(value = "/list")
    public TableDataInfo list(InventoryTransferMpt inventoryTransfer) {
        logger.info("管理后台用户[{}]分页查询移库信息", SecurityUtils.getUsername());
        startPage();
        List<InventoryTransferPo> inventoryTransferPoList = inventoryTransferAppService.search(inventoryTransfer.getVin(),
                inventoryTransfer.getWarehouseLevel(), inventoryTransfer.getWarehouseCode(), getBeginTime(inventoryTransfer),
                getEndTime(inventoryTransfer));
        List<InventoryTransferMpt> inventoryTransferMptList = InventoryTransferMptAssembler.INSTANCE.fromPoList(inventoryTransferPoList);
        return getDataTable(inventoryTransferPoList, inventoryTransferMptList);
    }

    /**
     * 导出移库信息
     *
     * @param response          响应
     * @param inventoryTransfer 移库信息
     */
    @Log(title = "移库信息管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, InventoryTransferMpt inventoryTransfer) {
        logger.info("管理后台用户[{}]导出移库信息", SecurityUtils.getUsername());
    }

    /**
     * 根据移库信息ID获取移库信息
     *
     * @param inventoryTransferId 移库信息ID
     * @return 移库信息
     */
    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:query")
    @Override
    @GetMapping(value = "/{inventoryTransferId}")
    public AjaxResult getInfo(@PathVariable Long inventoryTransferId) {
        logger.info("管理后台用户[{}]根据移库信息ID[{}]获取移库信息", SecurityUtils.getUsername(), inventoryTransferId);
        InventoryTransferPo inventoryTransferPo = inventoryTransferAppService.getInventoryTransferById(inventoryTransferId);
        return success(InventoryTransferMptAssembler.INSTANCE.fromPo(inventoryTransferPo));
    }

    /**
     * 新增移库信息
     *
     * @param inventoryTransfer 移库信息
     * @return 结果
     */
    @Log(title = "移库信息管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:add")
    @Override
    @PostMapping
    public AjaxResult add(@Validated @RequestBody InventoryTransferMpt inventoryTransfer) {
        logger.info("管理后台用户[{}]新增车辆[{}]移库信息", SecurityUtils.getUsername(), inventoryTransfer.getVin());
        InventoryTransferPo inventoryTransferPo = InventoryTransferMptAssembler.INSTANCE.toPo(inventoryTransfer);
        inventoryTransferPo.setCreateBy(SecurityUtils.getUserId().toString());
        return toAjax(inventoryTransferAppService.createInventoryTransfer(inventoryTransferPo));
    }

    /**
     * 修改移库信息
     *
     * @param inventoryTransfer 移库信息
     * @return 结果
     */
    @Log(title = "移库信息管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:edit")
    @Override
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody InventoryTransferMpt inventoryTransfer) {
        logger.info("管理后台用户[{}]修改保存车辆[{}]移库信息", SecurityUtils.getUsername(), inventoryTransfer.getVin());
        InventoryTransferPo inventoryTransferPo = InventoryTransferMptAssembler.INSTANCE.toPo(inventoryTransfer);
        inventoryTransferPo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(inventoryTransferAppService.modifyInventoryTransfer(inventoryTransferPo));
    }

    /**
     * 删除移库信息
     *
     * @param inventoryTransferIds 移库信息ID数组
     * @return 结果
     */
    @Log(title = "移库信息管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:remove")
    @Override
    @DeleteMapping("/{inventoryTransferIds}")
    public AjaxResult remove(@PathVariable Long[] inventoryTransferIds) {
        logger.info("管理后台用户[{}]删除移库信息[{}]", SecurityUtils.getUsername(), inventoryTransferIds);
        return toAjax(inventoryTransferAppService.deleteInventoryTransferByIds(inventoryTransferIds));
    }

}
