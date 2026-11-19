package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.common.enums.InboundTaskState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.InboundTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 交付库待入库任务聚合单元测试（OTD-WMS-DSN-CR-003）
 *
 * @author hwyz_leo
 */
@DisplayName("待入库任务聚合 - 状态机与聚合版本")
class InboundTaskTest {

    private InboundTask pending() {
        return InboundTask.createPending("IA1", "R1", "TMS", "TO-1", "VIN1", "CFG1", "DDC001",
                null, 2, new Date());
    }

    @Test
    @DisplayName("创建：PENDING，聚合版本为 1")
    void shouldCreatePending() {
        InboundTask task = pending();
        assertThat(task.getState()).isEqualTo(InboundTaskState.PENDING);
        assertThat(task.getAggregateVersion()).isEqualTo(1L);
        assertThat(task.getLifecycleSeq()).isEqualTo(2);
        assertThat(task.getWarehouseCode()).isEqualTo("DDC001");
    }

    @Test
    @DisplayName("到货确认：PENDING → ARRIVED，递增聚合版本")
    void shouldArrive() {
        InboundTask task = pending();
        task.arrival("op", new Date());
        assertThat(task.getState()).isEqualTo(InboundTaskState.ARRIVED);
        assertThat(task.getAggregateVersion()).isEqualTo(2L);
        assertThat(task.getArrivalTime()).isNotNull();
        assertThatThrownBy(() -> task.arrival("op", new Date()))
                .isInstanceOf(WmsBusinessException.class);
    }

    @Test
    @DisplayName("入库确认：仅 ARRIVED 可入库，完成后 COMPLETED")
    void shouldInboundOnlyFromArrived() {
        InboundTask task = pending();
        assertThatThrownBy(() -> task.inbound("op", new Date()))
                .isInstanceOf(WmsBusinessException.class);
        task.arrival("op", new Date());
        task.inbound("op", new Date());
        assertThat(task.getState()).isEqualTo(InboundTaskState.COMPLETED);
        assertThat(task.getInboundTime()).isNotNull();
        assertThatThrownBy(() -> task.cancel("op", "r", new Date()))
                .isInstanceOf(WmsBusinessException.class);
    }

    @Test
    @DisplayName("取消：PENDING/ARRIVED 允许，COMPLETED 拒绝")
    void shouldCancelBeforeInbound() {
        InboundTask task = pending();
        task.cancel("op", "客户改期", new Date());
        assertThat(task.getState()).isEqualTo(InboundTaskState.CANCELLED);
        assertThat(task.getCancelReason()).isEqualTo("客户改期");

        InboundTask arrived = pending();
        arrived.arrival("op", new Date());
        arrived.cancel("op", "取消", new Date());
        assertThat(arrived.getState()).isEqualTo(InboundTaskState.CANCELLED);
    }
}
