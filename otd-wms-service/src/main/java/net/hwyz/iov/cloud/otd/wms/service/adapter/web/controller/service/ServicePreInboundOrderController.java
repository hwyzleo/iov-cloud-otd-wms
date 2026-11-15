package net.hwyz.iov.cloud.otd.wms.service.adapter.web.controller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderExService;
import net.hwyz.iov.cloud.otd.wms.service.adapter.web.WebOperators;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CreatePreInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.service.PreInboundOrderAppService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预入库单相关服务接口实现类（存量同步接口）
 * <p>
 * 该入口保留兼容，不是 VMD 集成路径；VMD 只能通过 Kafka 生产事实接入。
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/service/preInboundOrder")
public class ServicePreInboundOrderController {

    private final PreInboundOrderAppService preInboundOrderAppService;

    /**
     * 创建车辆预入库单
     *
     * @param preInboundOrder 预入库单
     */
    @PostMapping()
    void createOrder(@RequestBody @Validated PreInboundOrderExService preInboundOrder) {
        logger.info("存量同步接口创建车辆[{}]预入库单", preInboundOrder.getVin());
        preInboundOrderAppService.createPreInbound(new CreatePreInboundCmd(preInboundOrder.getVin(),
                preInboundOrder.getBuildConfigCode(), preInboundOrder.getWarehouseCode(),
                preInboundOrder.getWarehouseLevel(), preInboundOrder.getEstimatedArrivalTime(),
                preInboundOrder.getEstimatedInboundTime(), WebOperators.currentOperator()));
    }
}
