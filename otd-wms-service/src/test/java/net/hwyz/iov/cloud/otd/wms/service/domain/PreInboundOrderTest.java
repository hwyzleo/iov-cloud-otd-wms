package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.common.enums.PreInboundState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 预入库聚合状态规则测试
 *
 * @author hwyz_leo
 */
class PreInboundOrderTest {

    private PreInboundOrder order(PreInboundState state) {
        return PreInboundOrder.builder()
                .orderNum("PIO1")
                .vin("VIN1")
                .warehouseCode("WH1")
                .state(state)
                .rowVersion(1)
                .build();
    }

    @Test
    void shouldConfirmArrivalFromPendingArrival() {
        PreInboundOrder order = order(PreInboundState.PENDING_ARRIVAL);
        Date now = new Date();
        order.confirmArrival(now);
        assertThat(order.getState()).isEqualTo(PreInboundState.ARRIVED);
        assertThat(order.getArrivalTime()).isEqualTo(now);
    }

    @Test
    void shouldRejectArrivalWhenNotPendingArrival() {
        assertThatThrownBy(() -> order(PreInboundState.ARRIVED).confirmArrival(new Date()))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.PRE_INBOUND_STATE_CONFLICT.getCode());
    }

    @Test
    void shouldConfirmInboundOnlyWhenArrived() {
        PreInboundOrder arrived = order(PreInboundState.ARRIVED);
        arrived.confirmInbound("9", new Date());
        assertThat(arrived.getState()).isEqualTo(PreInboundState.COMPLETED);
        assertThat(arrived.getInboundBy()).isEqualTo("9");

        assertThatThrownBy(() -> order(PreInboundState.PENDING_ARRIVAL).confirmInbound("9", new Date()))
                .isInstanceOf(WmsBusinessException.class);
    }

    @Test
    void shouldCancelOnlyBeforeCompletion() {
        PreInboundOrder pending = order(PreInboundState.PENDING_ARRIVAL);
        pending.cancel();
        assertThat(pending.getState()).isEqualTo(PreInboundState.CANCELLED);

        assertThatThrownBy(() -> order(PreInboundState.COMPLETED).cancel())
                .isInstanceOf(WmsBusinessException.class);
    }
}
