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
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountDetailMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryCountMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.InventoryCountMptApi;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.PageResults;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.WebOperators;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler.MptVoAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InventoryCountAppService;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryCountDetailPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryCountPo;
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
public class MptInventoryCountController extends BaseController implements InventoryCountMptApi {

    private final InventoryCountAppService inventoryCountAppService;

    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:list")
    @Override
    @GetMapping(value = "/list")
    public ApiResponse<PageResult<InventoryCountMpt>> list(InventoryCountMpt inventoryCount) {
        logger.info("管理后台用户[{}]分页查询盘点信息", WebOperators.currentOperator());
        startPage();
        List<InventoryCountPo> list = inventoryCountAppService.search(inventoryCount.getOrderNum(),
                inventoryCount.getType(), inventoryCount.getWarehouseLevel(), inventoryCount.getWarehouseCode(),
                inventoryCount.getState(), getBeginTime(inventoryCount), getEndTime(inventoryCount));
        return ApiResponse.ok(PageResults.of(list, MptVoAssembler::fromInventoryCount));
    }

    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:list")
    @Override
    @GetMapping(value = "/{inventoryCountOrderNum}/list")
    public ApiResponse<PageResult<InventoryCountDetailMpt>> listDetail(@PathVariable String inventoryCountOrderNum,
                                                                      InventoryCountDetailMpt inventoryCountDetail) {
        logger.info("管理后台用户[{}]分页查询盘点[{}]明细", WebOperators.currentOperator(), inventoryCountOrderNum);
        startPage();
        List<InventoryCountDetailPo> list = inventoryCountAppService.searchDetail(inventoryCountOrderNum,
                inventoryCountDetail.getWarehouseCode(), inventoryCountDetail.getStorageAreaCode(),
                getBeginTime(inventoryCountDetail), getEndTime(inventoryCountDetail));
        return ApiResponse.ok(PageResults.of(list, MptVoAssembler::fromInventoryCountDetail));
    }

    @Log(title = "盘点信息管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, InventoryCountMpt inventoryCount) {
        logger.info("管理后台用户[{}]导出盘点信息", WebOperators.currentOperator());
    }

    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:query")
    @Override
    @GetMapping(value = "/{inventoryCountId}")
    public ApiResponse<InventoryCountMpt> getInfo(@PathVariable Long inventoryCountId) {
        logger.info("管理后台用户[{}]根据盘点信息ID[{}]获取盘点信息", WebOperators.currentOperator(), inventoryCountId);
        return ApiResponse.ok(MptVoAssembler.fromInventoryCount(
                inventoryCountAppService.getInventoryCountById(inventoryCountId)));
    }

    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:query")
    @Override
    @GetMapping(value = "/{inventoryCountOrderNum}/{inventoryCountDetailId}")
    public ApiResponse<InventoryCountDetailMpt> getDetailInfo(@PathVariable String inventoryCountOrderNum,
                                                             @PathVariable Long inventoryCountDetailId) {
        logger.info("管理后台用户[{}]根据盘点[{}]明细ID[{}]获取盘点明细", WebOperators.currentOperator(),
                inventoryCountOrderNum, inventoryCountDetailId);
        return ApiResponse.ok(MptVoAssembler.fromInventoryCountDetail(
                inventoryCountAppService.getInventoryCountDetailByOrderNumAndId(inventoryCountOrderNum,
                        inventoryCountDetailId)));
    }

    @Log(title = "盘点信息管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:add")
    @Override
    @PostMapping
    public ApiResponse<Void> add(@Validated @RequestBody InventoryCountMpt inventoryCount) {
        logger.info("管理后台用户[{}]新增仓库[{}]盘点信息", WebOperators.currentOperator(),
                inventoryCount.getWarehouseCode());
        InventoryCountPo po = MptVoAssembler.toPo(inventoryCount);
        po.setCreateBy(WebOperators.currentOperator());
        return inventoryCountAppService.createInventoryCount(po) > 0
                ? ApiResponse.ok() : ApiResponse.fail("新增盘点信息失败");
    }

    @Log(title = "盘点信息管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:add")
    @Override
    @PostMapping("/{inventoryCountOrderNum}")
    public ApiResponse<Void> addDetail(@PathVariable String inventoryCountOrderNum,
                                       @Validated @RequestBody InventoryCountDetailMpt inventoryCountDetail) {
        logger.info("管理后台用户[{}]新增仓库盘点信息[{}]明细", WebOperators.currentOperator(), inventoryCountOrderNum);
        InventoryCountDetailPo po = MptVoAssembler.toPo(inventoryCountDetail);
        po.setOrderNum(inventoryCountOrderNum);
        po.setCreateBy(WebOperators.currentOperator());
        return inventoryCountAppService.createInventoryCountDetail(po) > 0
                ? ApiResponse.ok() : ApiResponse.fail("新增盘点明细失败");
    }

    @Log(title = "盘点信息管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:edit")
    @Override
    @PutMapping
    public ApiResponse<Void> edit(@Validated @RequestBody InventoryCountMpt inventoryCount) {
        logger.info("管理后台用户[{}]修改保存仓库[{}]盘点信息", WebOperators.currentOperator(),
                inventoryCount.getWarehouseCode());
        InventoryCountPo po = MptVoAssembler.toPo(inventoryCount);
        po.setModifyBy(WebOperators.currentOperator());
        return inventoryCountAppService.modifyInventoryCount(po) > 0
                ? ApiResponse.ok() : ApiResponse.fail("修改盘点信息失败");
    }

    @Log(title = "盘点信息管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:edit")
    @Override
    @PutMapping("/{inventoryCountOrderNum}")
    public ApiResponse<Void> editDetail(@PathVariable String inventoryCountOrderNum,
                                        @Validated @RequestBody InventoryCountDetailMpt inventoryCountDetail) {
        logger.info("管理后台用户[{}]修改保存仓库盘点信息[{}]明细", WebOperators.currentOperator(),
                inventoryCountOrderNum);
        InventoryCountDetailPo po = MptVoAssembler.toPo(inventoryCountDetail);
        po.setOrderNum(inventoryCountOrderNum);
        po.setModifyBy(WebOperators.currentOperator());
        return inventoryCountAppService.modifyInventoryCountDetail(po) > 0
                ? ApiResponse.ok() : ApiResponse.fail("修改盘点明细失败");
    }

    @Log(title = "盘点信息管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:remove")
    @Override
    @DeleteMapping("/{inventoryCountIds}")
    public ApiResponse<Void> remove(@PathVariable Long[] inventoryCountIds) {
        logger.info("管理后台用户[{}]删除盘点信息[{}]", WebOperators.currentOperator(), inventoryCountIds);
        return inventoryCountAppService.deleteInventoryCountByIds(inventoryCountIds) > 0
                ? ApiResponse.ok() : ApiResponse.fail("删除盘点信息失败");
    }

    @Log(title = "盘点信息管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:warehouse:inventoryCount:remove")
    @Override
    @DeleteMapping("/{inventoryCountOrderNum}/{inventoryCountDetailIds}")
    public ApiResponse<Void> removeDetail(@PathVariable String inventoryCountOrderNum,
                                          @PathVariable Long[] inventoryCountDetailIds) {
        logger.info("管理后台用户[{}]删除盘点信息[{}]下明细[{}]", WebOperators.currentOperator(),
                inventoryCountOrderNum, inventoryCountDetailIds);
        return inventoryCountAppService.deleteInventoryCountDetailByIds(inventoryCountOrderNum,
                inventoryCountDetailIds) > 0 ? ApiResponse.ok() : ApiResponse.fail("删除盘点明细失败");
    }
}
