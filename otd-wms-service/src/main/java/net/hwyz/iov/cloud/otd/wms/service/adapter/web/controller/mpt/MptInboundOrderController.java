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
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.InboundOrderMptApi;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.PageResults;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.WebOperators;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler.MptVoAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.WmsResultAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboundOrderFact;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InboundOrderCriteria;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入库单相关管理接口实现类
 * <p>
 * 入库单为不可变的已完成事实：仅提供查询与导出。
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/inboundOrder")
public class MptInboundOrderController extends BaseController implements InboundOrderMptApi {

    private final InboundOrderAppService inboundOrderAppService;

    @RequiresPermissions("completeVehicle:warehouse:inboundOrder:list")
    @Override
    @GetMapping(value = "/list")
    public ApiResponse<PageResult<InboundOrderMpt>> list(InboundOrderMpt inboundOrder) {
        log.info("管理后台用户[{}]分页查询入库单", WebOperators.currentOperator());
        startPage();
        List<InboundOrderFact> list = inboundOrderAppService.search(new InboundOrderCriteria(
                inboundOrder.getOrderNum(), inboundOrder.getPreOrderNum(), inboundOrder.getVin(),
                inboundOrder.getWarehouseLevel(), inboundOrder.getWarehouseCode(),
                getBeginTime(inboundOrder), getEndTime(inboundOrder)));
        return ApiResponse.ok(PageResults.of(WmsResultAssembler.fromInboundList(list),
                MptVoAssembler::fromInbound));
    }

    @Log(title = "入库单管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:inboundOrder:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, InboundOrderMpt inboundOrder) {
        log.info("管理后台用户[{}]导出入库单", WebOperators.currentOperator());
    }

    @RequiresPermissions("completeVehicle:warehouse:inboundOrder:query")
    @Override
    @GetMapping(value = "/{inboundOrderId}")
    public ApiResponse<InboundOrderMpt> getInfo(@PathVariable Long inboundOrderId) {
        log.info("管理后台用户[{}]根据入库单ID[{}]获取入库单", WebOperators.currentOperator(), inboundOrderId);
        InboundOrderFact fact = inboundOrderAppService.getById(inboundOrderId);
        return ApiResponse.ok(MptVoAssembler.fromInbound(WmsResultAssembler.fromInbound(fact)));
    }
}
