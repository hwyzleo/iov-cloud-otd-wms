package net.hwyz.iov.cloud.otd.wms.api.feign.service;

import net.hwyz.iov.cloud.framework.common.constant.ServiceNameConstants;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderExService;
import net.hwyz.iov.cloud.otd.wms.api.feign.service.factory.ExPreInboundOrderServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 预入库单相关服务接口
 *
 * @author hwyz_leo
 */
@FeignClient(contextId = "exPreInboundOrderService", value = ServiceNameConstants.OTD_WMS, path = "/service/preInboundOrder", fallbackFactory = ExPreInboundOrderServiceFallbackFactory.class)
public interface ExPreInboundOrderService {

    /**
     * 创建车辆预入库单
     *
     * @param preInboundOrder 预入库单
     */
    @PostMapping()
    void createOrder(@RequestBody @Validated PreInboundOrderExService preInboundOrder);

}
