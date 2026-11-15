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
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundArrivalMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundCancellationMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundInboundMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.PreInboundOrderMptApi;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.PageResults;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.WebOperators;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler.MptVoAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.WmsResultAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CancelPreInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmArrivalCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.PreInboundOrderDto;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.PreInboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.PreInboundOrderCriteria;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预入库单相关管理接口实现类
 * <p>
 * 状态流转只通过显式业务动作完成，不提供通用新增／修改／删除。
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/preInboundOrder")
public class MptPreInboundOrderController extends BaseController implements PreInboundOrderMptApi {

    private final PreInboundOrderAppService preInboundOrderAppService;
    private final InboundOrderAppService inboundOrderAppService;

    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:list")
    @Override
    @GetMapping(value = "/list")
    public ApiResponse<PageResult<PreInboundOrderMpt>> list(PreInboundOrderMpt preInboundOrder) {
        log.info("管理后台用户[{}]分页查询预入库单", WebOperators.currentOperator());
        startPage();
        List<PreInboundOrder> list = preInboundOrderAppService.search(new PreInboundOrderCriteria(
                preInboundOrder.getOrderNum(), preInboundOrder.getVin(), preInboundOrder.getWarehouseLevel(),
                preInboundOrder.getWarehouseCode(), preInboundOrder.getState(),
                getBeginTime(preInboundOrder), getEndTime(preInboundOrder)));
        return ApiResponse.ok(PageResults.of(WmsResultAssembler.fromPreInboundList(list),
                MptVoAssembler::fromPreInbound));
    }

    @Log(title = "预入库单管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, PreInboundOrderMpt preInboundOrder) {
        log.info("管理后台用户[{}]导出预入库单", WebOperators.currentOperator());
    }

    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:query")
    @Override
    @GetMapping(value = "/{preInboundOrderId}")
    public ApiResponse<PreInboundOrderMpt> getInfo(@PathVariable Long preInboundOrderId) {
        log.info("管理后台用户[{}]根据预入库单ID[{}]获取预入库单", WebOperators.currentOperator(), preInboundOrderId);
        PreInboundOrder order = preInboundOrderAppService.getById(preInboundOrderId);
        return ApiResponse.ok(MptVoAssembler.fromPreInbound(WmsResultAssembler.fromPreInbound(order)));
    }

    @Log(title = "预入库单到达确认", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:arrival")
    @Override
    @PostMapping(value = "/{orderNum}/arrival")
    public ApiResponse<PreInboundOrderMpt> arrival(@PathVariable String orderNum,
                                                   @Validated @RequestBody PreInboundArrivalMptRequest request) {
        log.info("管理后台用户[{}]确认预入库单[{}]到达", WebOperators.currentOperator(), orderNum);
        PreInboundOrder order = inboundOrderAppService.confirmArrival(new ConfirmArrivalCmd(orderNum,
                request.getRequestId(), request.getExpectedVersion(), WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromPreInbound(WmsResultAssembler.fromPreInbound(order)));
    }

    @Log(title = "预入库单入库确认", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:inbound")
    @Override
    @PostMapping(value = "/{orderNum}/inbound")
    public ApiResponse<PreInboundOrderMpt> inbound(@PathVariable String orderNum,
                                                   @Validated @RequestBody PreInboundInboundMptRequest request) {
        log.info("管理后台用户[{}]确认预入库单[{}]入库至储位[{}/{}]", WebOperators.currentOperator(), orderNum,
                request.getStorageAreaCode(), request.getStorageLocationCode());
        PreInboundOrder order = inboundOrderAppService.confirmInbound(new ConfirmInboundCmd(orderNum,
                request.getRequestId(), request.getStorageAreaCode(), request.getStorageLocationCode(),
                request.getExpectedVersion(), WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromPreInbound(WmsResultAssembler.fromPreInbound(order)));
    }

    @Log(title = "预入库单取消", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:preInboundOrder:cancel")
    @Override
    @PostMapping(value = "/{orderNum}/cancellation")
    public ApiResponse<PreInboundOrderMpt> cancellation(@PathVariable String orderNum,
                                                        @Validated @RequestBody PreInboundCancellationMptRequest request) {
        log.info("管理后台用户[{}]取消预入库单[{}]", WebOperators.currentOperator(), orderNum);
        PreInboundOrder order = inboundOrderAppService.cancelPreInbound(new CancelPreInboundCmd(orderNum,
                request.getRequestId(), request.getReason(), request.getExpectedVersion(),
                WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromPreInbound(WmsResultAssembler.fromPreInbound(order)));
    }
}
