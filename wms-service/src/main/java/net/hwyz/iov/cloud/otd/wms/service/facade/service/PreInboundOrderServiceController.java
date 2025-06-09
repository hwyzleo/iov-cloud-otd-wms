package net.hwyz.iov.cloud.otd.wms.service.facade.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderExService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.PreInboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.facade.assembler.PreInboundOrderExServiceAssembler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预入库单相关服务接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/service/preInboundOrder")
public class PreInboundOrderServiceController {

    private final PreInboundOrderAppService preInboundOrderAppService;

    /**
     * 创建车辆预入库单
     *
     * @param preInboundOrder 预入库单
     */
    @PostMapping()
    void createOrder(@RequestBody @Validated PreInboundOrderExService preInboundOrder) {
        logger.info("创建车辆[{}]预入库单", preInboundOrder.getVin());
        preInboundOrderAppService.createPreInboundOrder(PreInboundOrderExServiceAssembler.INSTANCE.toPo(preInboundOrder));
    }

}
