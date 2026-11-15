package net.hwyz.iov.cloud.otd.wms.api.feign.service.factory;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.api.contract.PreInboundOrderExService;
import net.hwyz.iov.cloud.otd.wms.api.feign.service.ExPreInboundOrderService;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 预入库单相关服务降级处理
 *
 * @author hwyz_leo
 */
@Slf4j
@Component
public class ExPreInboundOrderServiceFallbackFactory implements FallbackFactory<ExPreInboundOrderService> {

    @Override
    public ExPreInboundOrderService create(Throwable throwable) {
        return new ExPreInboundOrderService() {
            @Override
            public void createOrder(PreInboundOrderExService preInboundOrder) {
                logger.error("预入库单服务创建车辆[{}]预入库单调用失败", preInboundOrder.getVin(), throwable);
            }
        };
    }
}
