package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleOutboundRequestedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.port.OutboundFactPublisher;
import net.hwyz.iov.cloud.otd.wms.service.application.service.MptActionGuard;
import net.hwyz.iov.cloud.otd.wms.service.application.service.OutboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.WarehouseResolver;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.ConsumeOutcome;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InboxProcessState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboundOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用出库命令原子预留与幂等测试
 *
 * @author hwyz_leo
 */
@ExtendWith(MockitoExtension.class)
class OutboundOrderAppServiceTest {

    @Mock
    private OutboundOrderRepository outboundOrderRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InboxRepository inboxRepository;
    @Mock
    private OutboundFactPublisher outboundFactPublisher;
    @Mock
    private WarehouseResolver warehouseResolver;
    @Mock
    private MptActionGuard mptActionGuard;

    private OutboundOrderAppService service;

    @BeforeEach
    void setUp() {
        service = new OutboundOrderAppService(outboundOrderRepository, inventoryRepository, inboxRepository,
                outboundFactPublisher, warehouseResolver, mptActionGuard);
    }

    private VehicleOutboundRequestedCmd cmd(List<String> vins) {
        return new VehicleOutboundRequestedCmd("EVT1", "wms.vehicle-outbound.requested", "DIGEST", "REQ1",
                "TMS", "SRC1", null, vins, "0");
    }

    private VehicleInventory inventory(String vin) {
        return VehicleInventory.builder()
                .vin(vin)
                .buildConfigCode("CFG")
                .warehouseCode("WH1")
                .storageAreaCode("A1")
                .storageLocationCode("L-" + vin)
                .state(InventoryState.AVAILABLE)
                .rowVersion(1)
                .build();
    }

    @Test
    void shouldRejectWholeBatchWhenOneVinNotAvailable() {
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(warehouseResolver.resolve(null)).thenReturn("WH1");
        when(outboundOrderRepository.findBySourceRequest(anyString(), anyString())).thenReturn(Optional.empty());
        when(outboundOrderRepository.findBySourceOrder(anyString(), anyString())).thenReturn(Optional.empty());
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.of(inventory("VIN1")));
        when(inventoryRepository.findByVin("VIN2")).thenReturn(Optional.empty());

        ConsumeResultDto result = service.consumeRequested(cmd(List.of("VIN1", "VIN2")));

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.BUSINESS_REJECTED);
        verify(outboundOrderRepository, never()).insert(any(OutboundOrder.class));
        verify(inventoryRepository, never()).reserve(anyString(), anyString());
        verify(outboundFactPublisher).publishRejected(eq("REQ1"), eq("TMS"), eq("SRC1"), eq(null),
                eq("302010"), any(), eq(0L));
        ArgumentCaptor<InboxRecord> captor = ArgumentCaptor.forClass(InboxRecord.class);
        verify(inboxRepository).insert(captor.capture());
        assertThat(captor.getValue().getProcessState()).isEqualTo(InboxProcessState.BUSINESS_REJECTED);
    }

    @Test
    void shouldReserveAllVinsAndPublishChangedFact() {
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(warehouseResolver.resolve(null)).thenReturn("WH1");
        when(outboundOrderRepository.findBySourceRequest(anyString(), anyString())).thenReturn(Optional.empty());
        when(outboundOrderRepository.findBySourceOrder(anyString(), anyString())).thenReturn(Optional.empty());
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.of(inventory("VIN1")));
        when(inventoryRepository.findByVin("VIN2")).thenReturn(Optional.of(inventory("VIN2")));
        when(inventoryRepository.reserve(anyString(), anyString())).thenReturn(1);

        ConsumeResultDto result = service.consumeRequested(cmd(List.of("VIN2", "VIN1")));

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.PROCESSED);
        verify(outboundOrderRepository).insert(any(OutboundOrder.class));
        verify(inventoryRepository).reserve(eq("VIN1"), anyString());
        verify(inventoryRepository).reserve(eq("VIN2"), anyString());
        verify(outboundFactPublisher).publishChanged(any(OutboundOrder.class), eq("REQ1"));
        verify(outboundFactPublisher, never()).publishRejected(anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), anyLong());
    }

    @Test
    void shouldAcknowledgeDuplicateEventWithoutWrites() {
        InboxRecord recorded = InboxRecord.builder()
                .consumerName("c")
                .topic("t")
                .eventId("EVT1")
                .processState(InboxProcessState.PROCESSED)
                .build();
        when(inboxRepository.find(anyString(), anyString(), eq("EVT1"))).thenReturn(Optional.of(recorded));

        ConsumeResultDto result = service.consumeRequested(cmd(List.of("VIN1")));

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.DUPLICATE);
        verify(outboundOrderRepository, never()).insert(any(OutboundOrder.class));
        verify(outboundFactPublisher, never()).publishChanged(any(OutboundOrder.class), anyString());
    }

    @Test
    void shouldDeadLetterWhenRequestIdReusedWithDifferentContent() {
        OutboundOrder existing = OutboundOrder.createReserved("REQ1", "TMS", "SRC1", "WH1", "OUT1",
                List.of(net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail.builder()
                        .orderNum("OUT1").vin("VIN9").warehouseCode("WH1").storageAreaCode("A1")
                        .storageLocationCode("L9")
                        .state(net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundDetailState.RESERVED)
                        .rowVersion(1).build()));
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(warehouseResolver.resolve(null)).thenReturn("WH1");
        when(outboundOrderRepository.findBySourceRequest("TMS", "REQ1")).thenReturn(Optional.of(existing));

        ConsumeResultDto result = service.consumeRequested(cmd(List.of("VIN1")));

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.DEAD_LETTER);
        verify(outboundOrderRepository, never()).insert(any(OutboundOrder.class));
    }
}
