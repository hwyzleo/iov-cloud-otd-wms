package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConsumeVehicleProducedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.service.PreInboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.WarehouseResolver;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.ConsumeOutcome;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InboxProcessState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.PreInboundState;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboxRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboxRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.PreInboundOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * VMD 生产事实幂等消费测试
 *
 * @author hwyz_leo
 */
@ExtendWith(MockitoExtension.class)
class PreInboundOrderAppServiceTest {

    private static final String TOPIC = "vmd.vehicle-produce";

    @Mock
    private PreInboundOrderRepository preInboundOrderRepository;
    @Mock
    private InboxRepository inboxRepository;
    @Mock
    private WarehouseResolver warehouseResolver;

    private PreInboundOrderAppService service;

    @BeforeEach
    void setUp() {
        service = new PreInboundOrderAppService(preInboundOrderRepository, inboxRepository, warehouseResolver);
    }

    private ConsumeVehicleProducedCmd cmd(String eventId) {
        return new ConsumeVehicleProducedCmd(eventId, TOPIC, "BATCH1", "VIN1", "CFG1", null, "DIGEST");
    }

    @Test
    void shouldAcknowledgeDuplicateEventIdWithoutCreatingOrder() {
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.of(
                InboxRecord.builder().eventId("EVT1").processState(InboxProcessState.PROCESSED).build()));

        ConsumeResultDto result = service.consumeVehicleProduced(cmd("EVT1"));

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.DUPLICATE);
        verify(preInboundOrderRepository, never()).insert(any(PreInboundOrder.class));
    }

    @Test
    void shouldCreatePendingArrivalOrderWithDefaultWarehouse() {
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(preInboundOrderRepository.findByVin("VIN1")).thenReturn(Optional.empty());
        when(warehouseResolver.resolve(null)).thenReturn("WH1");

        ConsumeResultDto result = service.consumeVehicleProduced(cmd("EVT2"));

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.PROCESSED);
        ArgumentCaptor<PreInboundOrder> captor = ArgumentCaptor.forClass(PreInboundOrder.class);
        verify(preInboundOrderRepository).insert(captor.capture());
        PreInboundOrder order = captor.getValue();
        assertThat(order.getState()).isEqualTo(PreInboundState.PENDING_ARRIVAL);
        assertThat(order.getBuildConfigCode()).isEqualTo("CFG1");
        assertThat(order.getWarehouseCode()).isEqualTo("WH1");
        assertThat(order.getSourceSystem()).isEqualTo("VMD");
        assertThat(order.getSourceEventId()).isEqualTo("EVT2");
        assertThat(order.getSourceBatchNum()).isEqualTo("BATCH1");
    }

    @Test
    void shouldReuseExistingOrderForEquivalentVmdReplay() {
        PreInboundOrder existing = PreInboundOrder.builder()
                .orderNum("PIO1").vin("VIN1").buildConfigCode("CFG1").warehouseCode("WH1")
                .state(PreInboundState.PENDING_ARRIVAL).build();
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(preInboundOrderRepository.findByVin("VIN1")).thenReturn(Optional.of(existing));

        ConsumeResultDto result = service.consumeVehicleProduced(cmd("EVT3"));

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.PROCESSED);
        assertThat(result.detail()).isEqualTo("REUSED");
        verify(preInboundOrderRepository, never()).insert(any(PreInboundOrder.class));
    }

    @Test
    void shouldDeadLetterWhenProductionIdentityConflicts() {
        PreInboundOrder existing = PreInboundOrder.builder()
                .orderNum("PIO1").vin("VIN1").buildConfigCode("OTHER").warehouseCode("WH1")
                .state(PreInboundState.PENDING_ARRIVAL).build();
        when(inboxRepository.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(preInboundOrderRepository.findByVin("VIN1")).thenReturn(Optional.of(existing));

        ConsumeResultDto result = service.consumeVehicleProduced(cmd("EVT4"));

        assertThat(result.outcome()).isEqualTo(ConsumeOutcome.DEAD_LETTER);
        ArgumentCaptor<InboxRecord> captor = ArgumentCaptor.forClass(InboxRecord.class);
        verify(inboxRepository).insert(captor.capture());
        assertThat(captor.getValue().getProcessState()).isEqualTo(InboxProcessState.DEAD_LETTERED);
        verify(preInboundOrderRepository, never()).insert(any(PreInboundOrder.class));
    }
}
