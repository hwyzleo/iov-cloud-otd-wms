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
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountDetailMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.InventoryCountMptApi;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InventoryCountAppService;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.InventoryCountDetailMptAssembler;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.InventoryCountMptAssembler;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryCountDetailPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryCountPo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 盘点相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/inventoryCount")
public class InventoryCountMptController extends BaseController implements InventoryCountMptApi {

    private final InventoryCountAppService inventoryCountAppService;

    /**
     * 分页查询盘点信息
     *
     * @param inventoryCount 盘点信息
     * @return 盘点信息列表
     */
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:list")
    @Override
    @GetMapping(value = "/list")
    public TableDataInfo list(InventoryCountMpt inventoryCount) {
        logger.info("管理后台用户[{}]分页查询盘点信息", SecurityUtils.getUsername());
        startPage();
        List<InventoryCountPo> inventoryCountPoList = inventoryCountAppService.search(inventoryCount.getOrderNum(),
                inventoryCount.getType(), inventoryCount.getWarehouseLevel(), inventoryCount.getWarehouseCode(),
                inventoryCount.getState(), getBeginTime(inventoryCount), getEndTime(inventoryCount));
        List<InventoryCountMpt> inventoryCountMptList = InventoryCountMptAssembler.INSTANCE.fromPoList(inventoryCountPoList);
        return getDataTable(inventoryCountPoList, inventoryCountMptList);
    }

    /**
     * 分页查询盘点明细
     *
     * @param inventoryCountOrderNum 盘点单号
     * @param inventoryCountDetail   盘点明细
     * @return 盘点明细列表
     */
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:list")
    @Override
    @GetMapping(value = "/{inventoryCountOrderNum}/list")
    public TableDataInfo listDetail(@PathVariable String inventoryCountOrderNum, InventoryCountDetailMpt inventoryCountDetail) {
        logger.info("管理后台用户[{}]分页查询盘点[{}]明细", SecurityUtils.getUsername(), inventoryCountOrderNum);
        startPage();
        List<InventoryCountDetailPo> inventoryCountDetailPoList = inventoryCountAppService.searchDetail(inventoryCountOrderNum,
                inventoryCountDetail.getWarehouseCode(), inventoryCountDetail.getStorageAreaCode(),
                getBeginTime(inventoryCountDetail), getEndTime(inventoryCountDetail));
        List<InventoryCountDetailMpt> inventoryCountDetailMptList = InventoryCountDetailMptAssembler.INSTANCE.fromPoList(inventoryCountDetailPoList);
        return getDataTable(inventoryCountDetailPoList, inventoryCountDetailMptList);
    }

    /**
     * 导出盘点信息
     *
     * @param response       响应
     * @param inventoryCount 盘点信息
     */
    @Log(title = "盘点信息管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, InventoryCountMpt inventoryCount) {
        logger.info("管理后台用户[{}]导出盘点信息", SecurityUtils.getUsername());
    }

    /**
     * 根据盘点信息ID获取盘点信息
     *
     * @param inventoryCountId 盘点信息ID
     * @return 盘点信息
     */
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:query")
    @Override
    @GetMapping(value = "/{inventoryCountId}")
    public AjaxResult getInfo(@PathVariable Long inventoryCountId) {
        logger.info("管理后台用户[{}]根据盘点信息ID[{}]获取盘点信息", SecurityUtils.getUsername(), inventoryCountId);
        InventoryCountPo inventoryCountPo = inventoryCountAppService.getInventoryCountById(inventoryCountId);
        return success(InventoryCountMptAssembler.INSTANCE.fromPo(inventoryCountPo));
    }

    /**
     * 根据盘点明细ID获取盘点明细
     *
     * @param inventoryCountOrderNum 盘点单号
     * @param inventoryCountDetailId 盘点明细ID
     * @return 盘点明细
     */
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:query")
    @Override
    @GetMapping(value = "/{inventoryCountOrderNum}/{inventoryCountDetailId}")
    public AjaxResult getDetailInfo(@PathVariable String inventoryCountOrderNum, @PathVariable Long inventoryCountDetailId) {
        logger.info("管理后台用户[{}]根据盘点[{}]明细ID[{}]获取盘点明细", SecurityUtils.getUsername(), inventoryCountOrderNum, inventoryCountDetailId);
        InventoryCountDetailPo inventoryCountDetailPo = inventoryCountAppService.getInventoryCountDetailByOrderNumAndId(inventoryCountOrderNum,
                inventoryCountDetailId);
        return success(InventoryCountDetailMptAssembler.INSTANCE.fromPo(inventoryCountDetailPo));
    }

    /**
     * 新增盘点信息
     *
     * @param inventoryCount 盘点信息
     * @return 结果
     */
    @Log(title = "盘点信息管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:add")
    @Override
    @PostMapping
    public AjaxResult add(@Validated @RequestBody InventoryCountMpt inventoryCount) {
        logger.info("管理后台用户[{}]新增仓库[{}]盘点信息", SecurityUtils.getUsername(), inventoryCount.getWarehouseCode());
        InventoryCountPo inventoryCountPo = InventoryCountMptAssembler.INSTANCE.toPo(inventoryCount);
        inventoryCountPo.setCreateBy(SecurityUtils.getUserId().toString());
        return toAjax(inventoryCountAppService.createInventoryCount(inventoryCountPo));
    }

    /**
     * 新增盘点明细
     *
     * @param inventoryCountDetail 盘点明细
     * @return 结果
     */
    @Log(title = "盘点信息管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:add")
    @Override
    @PostMapping("/{inventoryCountOrderNum}")
    public AjaxResult addDetail(@PathVariable String inventoryCountOrderNum, @Validated @RequestBody InventoryCountDetailMpt inventoryCountDetail) {
        logger.info("管理后台用户[{}]新增仓库盘点信息[{}]明细", SecurityUtils.getUsername(), inventoryCountOrderNum);
        InventoryCountDetailPo inventoryCountDetailPo = InventoryCountDetailMptAssembler.INSTANCE.toPo(inventoryCountDetail);
        inventoryCountDetailPo.setCreateBy(SecurityUtils.getUserId().toString());
        return toAjax(inventoryCountAppService.createInventoryCountDetail(inventoryCountDetailPo));
    }

    /**
     * 修改盘点信息
     *
     * @param inventoryCount 盘点信息
     * @return 结果
     */
    @Log(title = "盘点信息管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:edit")
    @Override
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody InventoryCountMpt inventoryCount) {
        logger.info("管理后台用户[{}]修改保存仓库[{}]盘点信息", SecurityUtils.getUsername(), inventoryCount.getWarehouseCode());
        InventoryCountPo inventoryCountPo = InventoryCountMptAssembler.INSTANCE.toPo(inventoryCount);
        inventoryCountPo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(inventoryCountAppService.modifyInventoryCount(inventoryCountPo));
    }

    /**
     * 修改盘点明细
     *
     * @param inventoryCountOrderNum 盘点单号
     * @param inventoryCountDetail   盘点明细
     * @return 结果
     */
    @Log(title = "盘点信息管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:edit")
    @Override
    @PutMapping("/{inventoryCountOrderNum}")
    public AjaxResult editDetail(@PathVariable String inventoryCountOrderNum, @Validated @RequestBody InventoryCountDetailMpt inventoryCountDetail) {
        logger.info("管理后台用户[{}]修改保存仓库盘点信息[{}]明细", SecurityUtils.getUsername(), inventoryCountOrderNum);
        InventoryCountDetailPo inventoryCountDetailPo = InventoryCountDetailMptAssembler.INSTANCE.toPo(inventoryCountDetail);
        inventoryCountDetailPo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(inventoryCountAppService.modifyInventoryCountDetail(inventoryCountDetailPo));
    }

    /**
     * 删除盘点信息
     *
     * @param inventoryCountIds 盘点信息ID数组
     * @return 结果
     */
    @Log(title = "盘点信息管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:remove")
    @Override
    @DeleteMapping("/{inventoryCountIds}")
    public AjaxResult remove(@PathVariable Long[] inventoryCountIds) {
        logger.info("管理后台用户[{}]删除盘点信息[{}]", SecurityUtils.getUsername(), inventoryCountIds);
        return toAjax(inventoryCountAppService.deleteInventoryCountByIds(inventoryCountIds));
    }

    /**
     * 删除盘点明细
     *
     * @param inventoryCountOrderNum  盘点单号
     * @param inventoryCountDetailIds 盘点明细ID数组
     * @return 结果
     */
    @Log(title = "盘点信息管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:remove")
    @Override
    @DeleteMapping("/{inventoryCountOrderNum}/{inventoryCountDetailIds}")
    public AjaxResult removeDetail(@PathVariable String inventoryCountOrderNum, @PathVariable Long[] inventoryCountDetailIds) {
        logger.info("管理后台用户[{}]删除盘点信息[{}]下明细[{}]", SecurityUtils.getUsername(), inventoryCountOrderNum, inventoryCountDetailIds);
        return toAjax(inventoryCountAppService.deleteInventoryCountDetailByIds(inventoryCountOrderNum, inventoryCountDetailIds));
    }
}
