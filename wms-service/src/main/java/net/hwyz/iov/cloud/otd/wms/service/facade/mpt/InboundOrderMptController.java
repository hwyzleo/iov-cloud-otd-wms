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
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.InboundOrderMptApi;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.InboundOrderMptAssembler;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InboundOrderPo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入库单相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/inboundOrder")
public class InboundOrderMptController extends BaseController implements InboundOrderMptApi {

    private final InboundOrderAppService inboundOrderAppService;

    /**
     * 分页查询入库单
     *
     * @param inboundOrder 入库单
     * @return 入库单列表
     */
    @RequiresPermissions("completeVehicle:warehouse:inboundOrder:list")
    @Override
    @GetMapping(value = "/list")
    public TableDataInfo list(InboundOrderMpt inboundOrder) {
        logger.info("管理后台用户[{}]分页查询入库单", SecurityUtils.getUsername());
        startPage();
        List<InboundOrderPo> inboundOrderPoList = inboundOrderAppService.search(inboundOrder.getOrderNum(),
                inboundOrder.getVin(), inboundOrder.getWarehouseLevel(), inboundOrder.getWarehouseCode(),
                getBeginTime(inboundOrder), getEndTime(inboundOrder));
        List<InboundOrderMpt> inboundOrderMptList = InboundOrderMptAssembler.INSTANCE.fromPoList(inboundOrderPoList);
        return getDataTable(inboundOrderPoList, inboundOrderMptList);
    }

    /**
     * 导出入库单
     *
     * @param response     响应
     * @param inboundOrder 入库单
     */
    @Log(title = "入库单管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:inboundOrder:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, InboundOrderMpt inboundOrder) {
        logger.info("管理后台用户[{}]导出入库单", SecurityUtils.getUsername());
    }

    /**
     * 根据入库单ID获取入库单
     *
     * @param inboundOrderId 入库单ID
     * @return 入库单
     */
    @RequiresPermissions("completeVehicle:warehouse:inboundOrder:query")
    @Override
    @GetMapping(value = "/{inboundOrderId}")
    public AjaxResult getInfo(@PathVariable Long inboundOrderId) {
        logger.info("管理后台用户[{}]根据预入库单[{}]获取入库单", SecurityUtils.getUsername(), inboundOrderId);
        InboundOrderPo inboundOrderPo = inboundOrderAppService.getInboundOrderById(inboundOrderId);
        return success(InboundOrderMptAssembler.INSTANCE.fromPo(inboundOrderPo));
    }

    /**
     * 新增入库单
     *
     * @param inboundOrder 入库单
     * @return 结果
     */
    @Log(title = "入库单管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:inboundOrder:add")
    @Override
    @PostMapping
    public AjaxResult add(@Validated @RequestBody InboundOrderMpt inboundOrder) {
        logger.info("管理后台用户[{}]新增车辆[{}]入库单", SecurityUtils.getUsername(), inboundOrder.getVin());
        InboundOrderPo inboundOrderPo = InboundOrderMptAssembler.INSTANCE.toPo(inboundOrder);
        inboundOrderPo.setCreateBy(SecurityUtils.getUserId().toString());
        return toAjax(inboundOrderAppService.createInboundOrder(inboundOrderPo));
    }

    /**
     * 修改入库单
     *
     * @param inboundOrder 入库单
     * @return 结果
     */
    @Log(title = "入库单管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:inboundOrder:edit")
    @Override
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody InboundOrderMpt inboundOrder) {
        logger.info("管理后台用户[{}]修改保存入库单[{}]", SecurityUtils.getUsername(), inboundOrder.getOrderNum());
        if (!inboundOrderAppService.checkOrderNumUnique(inboundOrder.getId(), inboundOrder.getOrderNum())) {
            return error("修改保存入库单'" + inboundOrder.getOrderNum() + "'失败，入库单号已存在");
        }
        InboundOrderPo inboundOrderPo = InboundOrderMptAssembler.INSTANCE.toPo(inboundOrder);
        inboundOrderPo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(inboundOrderAppService.modifyInboundOrder(inboundOrderPo));
    }

    /**
     * 删除入库单
     *
     * @param inboundOrderIds 入库单ID数组
     * @return 结果
     */
    @Log(title = "预入库单管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:warehouse:inboundOrder:remove")
    @Override
    @DeleteMapping("/{inboundOrderIds}")
    public AjaxResult remove(@PathVariable Long[] inboundOrderIds) {
        logger.info("管理后台用户[{}]删除预入库单[{}]", SecurityUtils.getUsername(), inboundOrderIds);
        return toAjax(inboundOrderAppService.deleteInboundOrderByIds(inboundOrderIds));
    }

}
