package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.MptActionGuard;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.PreInboundState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboundOrderFact;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.PreInboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.service.StorageHierarchyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 入库动作状态门禁与原子写入测试
 *
 * @author hwyz_leo
 */
@ExtendWith(MockitoExtension.class)
class InboundOrderAppServiceTest {

    @Mock
    private PreInboundOrderRepository preInboundOrderRepository;
    @Mock
    private InboundOrderRepository inboundOrderRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private StorageHierarchyValidator storageHierarchyValidator;
    @Mock
    private MptActionGuard mptActionGuard;

    private InboundOrderAppService service;

    @BeforeEach
    void setUp() {
        service = new InboundOrderAppService(preInboundOrderRepository, inboundOrderRepository,
                inventoryRepository, storageHierarchyValidator, mptActionGuard);
    }

    private PreInboundOrder order(PreInboundState state) {
        return PreInboundOrder.builder()
                .id(1L)
                .orderNum("PIO1")
                .vin("VIN1")
                .buildConfigCode("CFG")
                .warehouseCode("WH1")
                .state(state)
                .rowVersion(2)
                .build();
    }

    private ConfirmInboundCmd inboundCmd() {
        return new ConfirmInboundCmd("PIO1", "REQ1", "A1", "L1", 2, "9");
    }

    @Test
    void shouldRejectInboundWhenNotArrived() {
        when(preInboundOrderRepository.findByOrderNum("PIO1")).thenReturn(Optional.of(order(PreInboundState.PENDING_ARRIVAL)));
        when(mptActionGuard.alreadyProcessed(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.confirmInbound(inboundCmd()))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.PRE_INBOUND_STATE_CONFLICT.getCode());
        verify(inboundOrderRepository, never()).insert(any(InboundOrderFact.class));
    }

    @Test
    void shouldRejectInboundWhenLocationOccupied() {
        when(preInboundOrderRepository.findByOrderNum("PIO1")).thenReturn(Optional.of(order(PreInboundState.ARRIVED)));
        when(mptActionGuard.alreadyProcessed(anyString(), anyString())).thenReturn(false);
        when(inventoryRepository.isLocationOccupied("WH1", "L1")).thenReturn(true);

        assertThatThrownBy(() -> service.confirmInbound(inboundCmd()))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.STORAGE_LOCATION_OCCUPIED.getCode());
        verify(inboundOrderRepository, never()).insert(any(InboundOrderFact.class));
        verify(inventoryRepository, never()).insert(any(VehicleInventory.class));
    }

    @Test
    void shouldWriteFactInventoryAndTransitionAtomically() {
        when(preInboundOrderRepository.findByOrderNum("PIO1")).thenReturn(Optional.of(order(PreInboundState.ARRIVED)));
        when(mptActionGuard.alreadyProcessed(anyString(), anyString())).thenReturn(false);
        when(inventoryRepository.isLocationOccupied("WH1", "L1")).thenReturn(false);
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.empty());
        when(preInboundOrderRepository.updateState(any(PreInboundOrder.class), any(Integer.class),
                anyString(), anyString())).thenReturn(1);

        PreInboundOrder result = service.confirmInbound(inboundCmd());

        assertThat(result.getState()).isEqualTo(PreInboundState.COMPLETED);
        verify(inboundOrderRepository).insert(any(InboundOrderFact.class));
        verify(inventoryRepository).insert(any(VehicleInventory.class));
        verify(mptActionGuard).recordProcessed(anyString(), anyString(), anyString());
    }

    @Test
    void shouldRejectStaleVersionOnCasFailure() {
        when(preInboundOrderRepository.findByOrderNum("PIO1")).thenReturn(Optional.of(order(PreInboundState.ARRIVED)));
        when(mptActionGuard.alreadyProcessed(anyString(), anyString())).thenReturn(false);
        when(inventoryRepository.isLocationOccupied("WH1", "L1")).thenReturn(false);
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.empty());
        when(preInboundOrderRepository.updateState(any(PreInboundOrder.class), any(Integer.class),
                anyString(), anyString())).thenReturn(0);

        assertThatThrownBy(() -> service.confirmInbound(inboundCmd()))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.STALE_VERSION.getCode());
    }

    @Test
    void shouldReturnCompletedOrderIdempotently() {
        when(preInboundOrderRepository.findByOrderNum("PIO1")).thenReturn(Optional.of(order(PreInboundState.COMPLETED)));
        when(mptActionGuard.alreadyProcessed(anyString(), anyString())).thenReturn(false);

        PreInboundOrder result = service.confirmInbound(inboundCmd());

        assertThat(result.getState()).isEqualTo(PreInboundState.COMPLETED);
        verify(inboundOrderRepository, never()).insert(any(InboundOrderFact.class));
    }
}
