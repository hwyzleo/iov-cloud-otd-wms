package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundDetailState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundOrderState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 出库单聚合状态推导与聚合版本测试
 *
 * @author hwyz_leo
 */
class OutboundOrderTest {

    private OutboundOrderDetail detail(String vin) {
        return OutboundOrderDetail.builder()
                .orderNum("OUT1")
                .vin(vin)
                .warehouseCode("WH1")
                .storageAreaCode("A1")
                .storageLocationCode("L-" + vin)
                .state(OutboundDetailState.RESERVED)
                .reserveTime(new Date())
                .rowVersion(1)
                .build();
    }

    private OutboundOrder order() {
        return OutboundOrder.createReserved("REQ1", "TMS", "SRC1", "WH1", "OUT1",
                List.of(detail("VIN1"), detail("VIN2")));
    }

    @Test
    void shouldDeriveOrderStateAndIncrementAggregateVersion() {
        OutboundOrder order = order();
        assertThat(order.getState()).isEqualTo(OutboundOrderState.RESERVED);
        assertThat(order.getAggregateVersion()).isEqualTo(1L);

        order.loadVin("VIN1", "9", new Date());
        assertThat(order.getState()).isEqualTo(OutboundOrderState.PARTIALLY_LOADED);
        assertThat(order.getAggregateVersion()).isEqualTo(2L);

        order.loadVin("VIN2", "9", new Date());
        assertThat(order.getState()).isEqualTo(OutboundOrderState.LOADED);
        assertThat(order.getAggregateVersion()).isEqualTo(3L);

        order.outboundVin("VIN1", "9", new Date());
        assertThat(order.getState()).isEqualTo(OutboundOrderState.PARTIALLY_OUTBOUND);

        order.outboundVin("VIN2", "9", new Date());
        assertThat(order.getState()).isEqualTo(OutboundOrderState.COMPLETED);
        assertThat(order.getAggregateVersion()).isEqualTo(5L);
    }

    @Test
    void shouldRejectLoadingTwice() {
        OutboundOrder order = order();
        order.loadVin("VIN1", "9", new Date());
        assertThatThrownBy(() -> order.loadVin("VIN1", "9", new Date()))
                .isInstanceOf(WmsBusinessException.class);
    }

    @Test
    void shouldCancelBeforeLoadingAndReleaseAllDetails() {
        OutboundOrder order = order();
        order.cancel("9", new Date());
        assertThat(order.getState()).isEqualTo(OutboundOrderState.CANCELLED);
        assertThat(order.getDetails()).allMatch(d -> d.getState() == OutboundDetailState.RELEASED);
        assertThat(order.getAggregateVersion()).isEqualTo(2L);
    }

    @Test
    void shouldRejectLateCancellation() {
        OutboundOrder order = order();
        order.loadVin("VIN1", "9", new Date());
        long versionBefore = order.getAggregateVersion();
        assertThatThrownBy(() -> order.cancel("9", new Date())).isInstanceOf(WmsBusinessException.class);
        assertThat(order.getAggregateVersion()).isEqualTo(versionBefore);
        assertThat(order.getState()).isEqualTo(OutboundOrderState.PARTIALLY_LOADED);
    }
}
