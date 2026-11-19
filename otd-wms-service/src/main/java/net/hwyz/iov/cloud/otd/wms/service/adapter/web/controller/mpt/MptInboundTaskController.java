package net.hwyz.iov.cloud.otd.wms.service.adapter.web.controller.mpt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.audit.annotation.Log;
import net.hwyz.iov.cloud.framework.audit.enums.BusinessType;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundTaskActionMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundTaskCancellationMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundTaskInboundMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.contract.InboundTaskMpt;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.InboundTaskMptApi;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.PageResults;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.WebOperators;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler.MptVoAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.WmsResultAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ArrivalInboundTaskCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CancelInboundTaskCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmInboundTaskCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InboundTaskAppService;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.InboundTask;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InboundTaskCriteria;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 待入库任务相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/inboundTask")
public class MptInboundTaskController extends BaseController implements InboundTaskMptApi {

    private final InboundTaskAppService inboundTaskAppService;

    @RequiresPermissions("completeVehicle:warehouse:inboundTask:list")
    @Override
    @GetMapping(value = "/list")
    public ApiResponse<PageResult<InboundTaskMpt>> list(InboundTaskMpt inboundTask) {
        log.info("管理后台用户[{}]分页查询待入库任务", WebOperators.currentOperator());
        startPage();
        List<InboundTask> list = inboundTaskAppService.search(new InboundTaskCriteria(
                inboundTask.getOrderNum(), inboundTask.getSourceOrderNum(), inboundTask.getSourceSystem(),
                inboundTask.getWarehouseCode(), inboundTask.getState(), inboundTask.getVin(),
                getBeginTime(inboundTask), getEndTime(inboundTask)));
        return ApiResponse.ok(PageResults.of(WmsResultAssembler.fromInboundTaskList(list),
                MptVoAssembler::fromInboundTask));
    }

    @RequiresPermissions("completeVehicle:warehouse:inboundTask:query")
    @Override
    @GetMapping(value = "/{inboundTaskId}")
    public ApiResponse<InboundTaskMpt> getInfo(@PathVariable Long inboundTaskId) {
        log.info("管理后台用户[{}]根据任务ID[{}]获取待入库任务", WebOperators.currentOperator(), inboundTaskId);
        InboundTask task = inboundTaskAppService.getById(inboundTaskId);
        return ApiResponse.ok(MptVoAssembler.fromInboundTask(WmsResultAssembler.fromInboundTask(task)));
    }

    @Log(title = "待入库任务到货确认", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:inboundTask:arrival")
    @Override
    @PostMapping(value = "/{orderNum}/arrival")
    public ApiResponse<InboundTaskMpt> arrival(@PathVariable String orderNum,
                                               @Validated @RequestBody InboundTaskActionMptRequest request) {
        log.info("管理后台用户[{}]确认任务[{}]到货", WebOperators.currentOperator(), orderNum);
        InboundTask task = inboundTaskAppService.arrival(new ArrivalInboundTaskCmd(orderNum,
                request.getRequestId(), request.getExpectedVersion(), WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromInboundTask(WmsResultAssembler.fromInboundTask(task)));
    }

    @Log(title = "待入库任务入库确认", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:inboundTask:inbound")
    @Override
    @PostMapping(value = "/{orderNum}/inbound")
    public ApiResponse<InboundTaskMpt> inbound(@PathVariable String orderNum,
                                               @Validated @RequestBody InboundTaskInboundMptRequest request) {
        log.info("管理后台用户[{}]确认任务[{}]入库", WebOperators.currentOperator(), orderNum);
        InboundTask task = inboundTaskAppService.confirmInbound(new ConfirmInboundTaskCmd(orderNum,
                request.getRequestId(), request.getStorageAreaCode(), request.getStorageLocationCode(),
                request.getExpectedVersion(), WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromInboundTask(WmsResultAssembler.fromInboundTask(task)));
    }

    @Log(title = "待入库任务取消", businessType = BusinessType.UPDATE)
    @RequiresPermissions("completeVehicle:warehouse:inboundTask:cancel")
    @Override
    @PostMapping(value = "/{orderNum}/cancellation")
    public ApiResponse<InboundTaskMpt> cancellation(@PathVariable String orderNum,
                                                    @Validated @RequestBody InboundTaskCancellationMptRequest request) {
        log.info("管理后台用户[{}]取消待入库任务[{}]", WebOperators.currentOperator(), orderNum);
        InboundTask task = inboundTaskAppService.cancel(new CancelInboundTaskCmd(orderNum,
                request.getRequestId(), request.getReason(), request.getExpectedVersion(),
                WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromInboundTask(WmsResultAssembler.fromInboundTask(task)));
    }
}
