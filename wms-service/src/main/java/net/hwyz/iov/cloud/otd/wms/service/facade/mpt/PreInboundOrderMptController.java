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
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.PreInboundOrderMptApi;
import net.hwyz.iov.cloud.otd.wms.service.application.service.PreInboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.PreInboundOrderMptAssembler;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.PreInboundOrderPo;
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
@RequestMapping(value = "/mpt/preInboundOrder")
public class PreInboundOrderMptController extends BaseController implements PreInboundOrderMptApi {

    private final PreInboundOrderAppService preInboundOrderAppService;

    /**
     * 分页查询预入库单
     *
     * @param preInboundOrder 预入库单
     * @return 预入库单列表
     */
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:list")
    @Override
    @GetMapping(value = "/list")
    public TableDataInfo list(PreInboundOrderMpt preInboundOrder) {
        logger.info("管理后台用户[{}]分页查询预入库单", SecurityUtils.getUsername());
        startPage();
        List<PreInboundOrderPo> preInboundOrderPoList = preInboundOrderAppService.search(preInboundOrder.getOrderNum(),
                preInboundOrder.getVin(), preInboundOrder.getWarehouseLevel(), preInboundOrder.getWarehouseCode(),
                getBeginTime(preInboundOrder), getEndTime(preInboundOrder));
        List<PreInboundOrderMpt> preInboundOrderMptList = PreInboundOrderMptAssembler.INSTANCE.fromPoList(preInboundOrderPoList);
        return getDataTable(preInboundOrderPoList, preInboundOrderMptList);
    }

    /**
     * 导出预入库单
     *
     * @param response        响应
     * @param preInboundOrder 预入库单
     */
    @Log(title = "预入库单管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, PreInboundOrderMpt preInboundOrder) {
        logger.info("管理后台用户[{}]导出预入库单", SecurityUtils.getUsername());
    }

    /**
     * 根据预入库单ID获取预入库单
     *
     * @param preInboundOrderId 预入库单ID
     * @return 预入库单
     */
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:query")
    @Override
    @GetMapping(value = "/{preInboundOrderId}")
    public AjaxResult getInfo(@PathVariable Long preInboundOrderId) {
        logger.info("管理后台用户[{}]根据预入库单[{}]获取预入库单", SecurityUtils.getUsername(), preInboundOrderId);
        PreInboundOrderPo preInboundOrderPo = preInboundOrderAppService.getPreInboundOrderById(preInboundOrderId);
        return success(PreInboundOrderMptAssembler.INSTANCE.fromPo(preInboundOrderPo));
    }

    /**
     * 新增预入库单
     *
     * @param preInboundOrder 预入库单
     * @return 结果
     */
    @Log(title = "预入库单管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:add")
    @Override
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PreInboundOrderMpt preInboundOrder) {
        logger.info("管理后台用户[{}]新增预入库单[{}]", SecurityUtils.getUsername(), preInboundOrder.getOrderNum());
        PreInboundOrderPo preInboundOrderPo = PreInboundOrderMptAssembler.INSTANCE.toPo(preInboundOrder);
        preInboundOrderPo.setCreateBy(SecurityUtils.getUserId().toString());
        return toAjax(preInboundOrderAppService.createPreInboundOrder(preInboundOrderPo));
    }

    /**
     * 修改预入库单
     *
     * @param preInboundOrder 预入库单
     * @return 结果
     */
    @Log(title = "预入库单管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:edit")
    @Override
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PreInboundOrderMpt preInboundOrder) {
        logger.info("管理后台用户[{}]修改保存预入库单[{}]", SecurityUtils.getUsername(), preInboundOrder.getOrderNum());
        if (!preInboundOrderAppService.checkOrderNumUnique(preInboundOrder.getId(), preInboundOrder.getOrderNum())) {
            return error("修改保存预入库单'" + preInboundOrder.getOrderNum() + "'失败，预入库单号已存在");
        }
        PreInboundOrderPo preInboundOrderPo = PreInboundOrderMptAssembler.INSTANCE.toPo(preInboundOrder);
        preInboundOrderPo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(preInboundOrderAppService.modifyPreInboundOrder(preInboundOrderPo));
    }

    /**
     * 删除预入库单
     *
     * @param preInboundOrderIds 预入库单ID数组
     * @return 结果
     */
    @Log(title = "预入库单管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:remove")
    @Override
    @DeleteMapping("/{preInboundOrderIds}")
    public AjaxResult remove(@PathVariable Long[] preInboundOrderIds) {
        logger.info("管理后台用户[{}]删除预入库单[{}]", SecurityUtils.getUsername(), preInboundOrderIds);
        return toAjax(preInboundOrderAppService.deletePreInboundOrderByIds(preInboundOrderIds));
    }

}
