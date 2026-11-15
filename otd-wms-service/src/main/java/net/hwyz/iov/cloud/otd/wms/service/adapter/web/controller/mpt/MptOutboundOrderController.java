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
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundCancellationMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundOrderDetailMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.OutboundVinActionMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.OutboundOrderMptApi;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.PageResults;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.WebOperators;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler.MptVoAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.WmsResultAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CancelOutboundOrderCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.LoadVinCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.OutboundVinCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.service.OutboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.OutboundOrderCriteria;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 出库单相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/outboundOrder")
public class MptOutboundOrderController extends BaseController implements OutboundOrderMptApi {

    private final OutboundOrderAppService outboundOrderAppService;

    @RequiresPermissions("completeVehicle:warehouse:outboundOrder:list")
    @Override
    @GetMapping(value = "/list")
    public ApiResponse<PageResult<OutboundOrderMpt>> list(OutboundOrderMpt outboundOrder) {
        log.info("管理后台用户[{}]分页查询出库单", WebOperators.currentOperator());
        startPage();
        List<OutboundOrder> list = outboundOrderAppService.search(new OutboundOrderCriteria(
                outboundOrder.getOrderNum(), outboundOrder.getSourceSystem(), outboundOrder.getSourceOrderNum(),
                outboundOrder.getWarehouseCode(), outboundOrder.getState(),
                getBeginTime(outboundOrder), getEndTime(outboundOrder)));
        return ApiResponse.ok(PageResults.of(WmsResultAssembler.fromOutboundList(list),
                MptVoAssembler::fromOutbound));
    }

    @RequiresPermissions("completeVehicle:warehouse:outboundOrder:query")
    @Override
    @GetMapping(value = "/{outboundOrderId}")
    public ApiResponse<OutboundOrderMpt> getInfo(@PathVariable Long outboundOrderId) {
        log.info("管理后台用户[{}]根据出库单ID[{}]获取出库单", WebOperators.currentOperator(), outboundOrderId);
        OutboundOrder order = outboundOrderAppService.getById(outboundOrderId);
        return ApiResponse.ok(MptVoAssembler.fromOutbound(WmsResultAssembler.fromOutbound(order)));
    }

    @RequiresPermissions("completeVehicle:warehouse:outboundOrder:query")
    @Override
    @GetMapping(value = "/{outboundOrderId}/detail")
    public ApiResponse<List<OutboundOrderDetailMpt>> listDetail(@PathVariable Long outboundOrderId) {
        log.info("管理后台用户[{}]获取出库单ID[{}] VIN 明细", WebOperators.currentOperator(), outboundOrderId);
        List<OutboundOrderDetail> details = outboundOrderAppService.listDetails(outboundOrderId);
        return ApiResponse.ok(MptVoAssembler.fromOutboundDetailList(
                WmsResultAssembler.fromOutboundDetailList(details)));
    }

    @Log(title = "出库单装车确认", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:outboundOrder:load")
    @Override
    @PostMapping(value = "/{orderNum}/vins/{vin}/loading")
    public ApiResponse<OutboundOrderDetailMpt> loading(@PathVariable String orderNum, @PathVariable String vin,
                                                       @Validated @RequestBody OutboundVinActionMptRequest request) {
        log.info("管理后台用户[{}]确认出库单[{}]车辆[{}]装车", WebOperators.currentOperator(), orderNum, vin);
        OutboundOrderDetail detail = outboundOrderAppService.loadVin(new LoadVinCmd(orderNum, vin,
                request.getRequestId(), request.getExpectedVersion(), WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromOutboundDetail(WmsResultAssembler.fromOutboundDetail(detail)));
    }

    @Log(title = "出库单出库确认", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:outboundOrder:outbound")
    @Override
    @PostMapping(value = "/{orderNum}/vins/{vin}/outbound")
    public ApiResponse<OutboundOrderDetailMpt> outbound(@PathVariable String orderNum, @PathVariable String vin,
                                                        @Validated @RequestBody OutboundVinActionMptRequest request) {
        log.info("管理后台用户[{}]确认出库单[{}]车辆[{}]出库", WebOperators.currentOperator(), orderNum, vin);
        OutboundOrderDetail detail = outboundOrderAppService.outboundVin(new OutboundVinCmd(orderNum, vin,
                request.getRequestId(), request.getExpectedVersion(), WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromOutboundDetail(WmsResultAssembler.fromOutboundDetail(detail)));
    }

    @Log(title = "出库单取消", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:outboundOrder:cancel")
    @Override
    @PostMapping(value = "/{orderNum}/cancellation")
    public ApiResponse<OutboundOrderMpt> cancellation(@PathVariable String orderNum,
                                                      @Validated @RequestBody OutboundCancellationMptRequest request) {
        log.info("管理后台用户[{}]取消出库单[{}]", WebOperators.currentOperator(), orderNum);
        OutboundOrder order = outboundOrderAppService.cancel(new CancelOutboundOrderCmd(orderNum,
                request.getRequestId(), request.getReason(), request.getExpectedVersion(),
                WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromOutbound(WmsResultAssembler.fromOutbound(order)));
    }

    @Log(title = "出库单管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:outboundOrder:list")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, OutboundOrderMpt outboundOrder) {
        log.info("管理后台用户[{}]导出出库单", WebOperators.currentOperator());
    }
}
