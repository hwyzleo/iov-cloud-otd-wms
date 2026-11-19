package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ArrivalInboundTaskCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CancelInboundTaskCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmInboundTaskCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleInboundRequestedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.port.InboundFactPublisher;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InboundTaskAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.MptActionGuard;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.ConsumeOutcome;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InboundTaskState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.InboundTask;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboundTaskRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.service.StorageHierarchyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 交付库到货受理与入库动作测试（OTD-WMS-DSN-CR-003）
 *
 * @author hwyz_leo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("交付库待入库应用服务")
class InboundTaskAppServiceTest {

    @Mock
    private InboundTaskRepository inboundTaskRepository;
    @Mock
    private InboundOrderRepository inboundOrderRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InboxRepository inboxRepository;
    @Mock
    private InboundFactPublisher inboundFactPublisher;
    @Mock
    private StorageHierarchyValidator storageHierarchyValidator;
    @Mock
    private MptActionGuard mptActionGuard;

    private InboundTaskAppService service;

    @BeforeEach
    void setUp() {
        service = new InboundTaskAppService(inboundTaskRepository, inboundOrderRepository, inventoryRepository,
                inboxRepository, inboundFactPublisher, storageHierarchyValidator, mptActionGuard);
    }

    private VehicleInboundRequestedCmd command() {
        return new VehicleInboundRequestedCmd("E1", "wms.vehicle-inbound.requested", "digest", "R1", "TMS",
                "TO-1", "DDC001", List.of("VIN1"), null, "KAFKA_COMMAND");
    }

    private VehicleInventory outboundInventory() {
        return VehicleInventory.builder().vin("VIN1").buildConfigCode("CFG1").warehouseCode("PDC001")
                .state(InventoryState.OUTBOUND).lifecycleSeq(1).rowVersion(3).build();
    }

    private InboundTask arrivedTask() {
        InboundTask task = InboundTask.createPending("IA1", "R1", "TMS", "TO-1", "VIN1", "CFG1", "DDC001",
                null, 2, new Date());
        task.arrival("op", new Date());
        return task;
    }

    @Test
    @DisplayName("到货请求受理：生成任务并把库存置为 IN_TRANSIT")
    void shouldAcceptRequestedCommand() {
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(inboundTaskRepository.findBySourceRequest("TMS", "R1")).thenReturn(Optional.empty());
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.of(outboundInventory()));
        when(inventoryRepository.markInTransit("VIN1", "DDC001")).thenReturn(1);

        ConsumeResultDto result = service.consumeRequested(command());

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.PROCESSED);
        verify(inboundTaskRepository).insert(any(InboundTask.class));
        verify(inboundFactPublisher).publishChanged(any(InboundTask.class), eq("R1"));
        verify(inboxRepository).insert(any());
    }

    @Test
    @DisplayName("到货请求拒绝：VIN 不是 OUTBOUND 时整批拒绝且不建任务")
    void shouldRejectWhenVinNotOutbound() {
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(inboundTaskRepository.findBySourceRequest("TMS", "R1")).thenReturn(Optional.empty());
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.of(VehicleInventory.builder()
                .vin("VIN1").state(InventoryState.RESERVED).build()));

        ConsumeResultDto result = service.consumeRequested(command());

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.BUSINESS_REJECTED);
        verify(inboundTaskRepository, never()).insert(any());
        verify(inventoryRepository, never()).markInTransit(anyString(), anyString());
        verify(inboundFactPublisher).publishRejected(eq("R1"), eq("TMS"), eq("TO-1"),
                org.mockito.ArgumentMatchers.isNull(), anyString(), any(), eq(0L));
    }

    @Test
    @DisplayName("到货请求死信：缺少目标仓库时不落任务")
    void shouldDeadLetterWhenWarehouseMissing() {
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        VehicleInboundRequestedCmd cmd = new VehicleInboundRequestedCmd("E2", "t", "d", "R2", "TMS", "TO-2",
                null, List.of("VIN1"), null, "KAFKA_COMMAND");

        ConsumeResultDto result = service.consumeRequested(cmd);

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.DEAD_LETTER);
        verify(inboundTaskRepository, never()).insert(any());
    }

    @Test
    @DisplayName("到货确认：PENDING → ARRIVED 并发布事实")
    void shouldArrive() {
        InboundTask task = InboundTask.createPending("IA1", "R1", "TMS", "TO-1", "VIN1", "CFG1", "DDC001",
                null, 2, new Date());
        when(inboundTaskRepository.findByOrderNum("IA1")).thenReturn(Optional.of(task));
        when(mptActionGuard.alreadyProcessed(anyString(), anyString())).thenReturn(false);
        when(inboundTaskRepository.update(any(InboundTask.class), eq(1), eq("op"))).thenReturn(1);

        InboundTask result = service.arrival(new ArrivalInboundTaskCmd("IA1", "req-1", 1, "op"));

        assertThat(result.getState()).isEqualTo(InboundTaskState.ARRIVED);
        verify(inboundFactPublisher).publishChanged(any(InboundTask.class), eq("req-1"));
        verify(mptActionGuard).recordProcessed(anyString(), eq("req-1"), eq("IA1"));
    }

    @Test
    @DisplayName("入库确认：写入第二段入库事实、库存转 AVAILABLE 并完成任务")
    void shouldConfirmInboundWriteFact() {
        InboundTask task = arrivedTask();
        when(inboundTaskRepository.findByOrderNum("IA1")).thenReturn(Optional.of(task));
        when(mptActionGuard.alreadyProcessed(anyString(), anyString())).thenReturn(false);
        when(inventoryRepository.isLocationOccupied("DDC001", "L1")).thenReturn(false);
        when(inventoryRepository.confirmInbound(eq("VIN1"), eq("DDC001"), eq("A1"), eq("L1"), eq("op"), any()))
                .thenReturn(1);
        when(inboundTaskRepository.update(any(InboundTask.class), anyInt(), eq("op"))).thenReturn(1);

        InboundTask result = service.confirmInbound(new ConfirmInboundTaskCmd("IA1", "req-2", "A1", "L1", null,
                "op"));

        assertThat(result.getState()).isEqualTo(InboundTaskState.COMPLETED);
        verify(inboundOrderRepository).insert(any());
        verify(inboundFactPublisher).publishChanged(any(InboundTask.class), eq("req-2"));
    }

    @Test
    @DisplayName("取消：任务 CANCELLED 且库存回退 OUTBOUND")
    void shouldCancelAndReleaseInventory() {
        InboundTask task = InboundTask.createPending("IA1", "R1", "TMS", "TO-1", "VIN1", "CFG1", "DDC001",
                null, 2, new Date());
        when(inboundTaskRepository.findByOrderNum("IA1")).thenReturn(Optional.of(task));
        when(mptActionGuard.alreadyProcessed(anyString(), anyString())).thenReturn(false);
        when(inboundTaskRepository.update(any(InboundTask.class), anyInt(), eq("op"))).thenReturn(1);
        when(inventoryRepository.releaseInTransit("VIN1")).thenReturn(1);

        InboundTask result = service.cancel(new CancelInboundTaskCmd("IA1", "req-3", "改期", null, "op"));

        assertThat(result.getState()).isEqualTo(InboundTaskState.CANCELLED);
        verify(inventoryRepository).releaseInTransit("VIN1");
        verify(inboundFactPublisher, times(1)).publishChanged(any(InboundTask.class), eq("req-3"));
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }
}
