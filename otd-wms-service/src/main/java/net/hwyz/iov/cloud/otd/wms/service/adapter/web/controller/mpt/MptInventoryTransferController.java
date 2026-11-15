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
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryTransferMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryTransferMptRequest;
import net.hwyz.iov.cloud.otd.wms.api.feign.mpt.InventoryTransferMptApi;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.PageResults;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.WebOperators;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler.MptVoAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.assembler.WmsResultAssembler;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.TransferInventoryCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InventoryTransferAppService;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InventoryTransferRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InventoryTransferCriteria;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 移库相关管理接口实现类
 * <p>
 * 移储事实不可变：仅提供查询、导出与同仓移储动作。
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/inventoryTransfer")
public class MptInventoryTransferController extends BaseController implements InventoryTransferMptApi {

    private final InventoryTransferAppService inventoryTransferAppService;

    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:list")
    @Override
    @GetMapping(value = "/list")
    public ApiResponse<PageResult<InventoryTransferMpt>> list(InventoryTransferMpt inventoryTransfer) {
        logger.info("管理后台用户[{}]分页查询移库信息", WebOperators.currentOperator());
        startPage();
        List<InventoryTransferRecord> list = inventoryTransferAppService.search(new InventoryTransferCriteria(
                inventoryTransfer.getVin(), inventoryTransfer.getWarehouseLevel(),
                inventoryTransfer.getWarehouseCode(), getBeginTime(inventoryTransfer),
                getEndTime(inventoryTransfer)));
        return ApiResponse.ok(PageResults.of(WmsResultAssembler.fromTransferList(list),
                MptVoAssembler::fromTransfer));
    }

    @Log(title = "移库信息管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, InventoryTransferMpt inventoryTransfer) {
        logger.info("管理后台用户[{}]导出移库信息", WebOperators.currentOperator());
    }

    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:query")
    @Override
    @GetMapping(value = "/{inventoryTransferId}")
    public ApiResponse<InventoryTransferMpt> getInfo(@PathVariable Long inventoryTransferId) {
        logger.info("管理后台用户[{}]根据移库信息ID[{}]获取移库信息", WebOperators.currentOperator(), inventoryTransferId);
        InventoryTransferRecord record = inventoryTransferAppService.getById(inventoryTransferId);
        return ApiResponse.ok(MptVoAssembler.fromTransfer(WmsResultAssembler.fromTransfer(record)));
    }

    @Log(title = "移库信息管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("completeVehicle:warehouse:inventoryTransfer:add")
    @Override
    @PostMapping
    public ApiResponse<InventoryTransferMpt> transfer(@Validated @RequestBody InventoryTransferMptRequest request) {
        logger.info("管理后台用户[{}]车辆[{}]同仓移储至[{}/{}]", WebOperators.currentOperator(), request.getVin(),
                request.getNewStorageAreaCode(), request.getNewStorageLocationCode());
        InventoryTransferRecord record = inventoryTransferAppService.transfer(new TransferInventoryCmd(
                request.getRequestId(), request.getVin(), request.getWarehouseCode(),
                request.getNewStorageAreaCode(), request.getNewStorageLocationCode(),
                request.getExpectedVersion(), WebOperators.currentOperator()));
        return ApiResponse.ok(MptVoAssembler.fromTransfer(WmsResultAssembler.fromTransfer(record)));
    }
}
