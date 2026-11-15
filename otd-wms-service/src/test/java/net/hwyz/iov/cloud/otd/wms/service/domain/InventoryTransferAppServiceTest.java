package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.TransferInventoryCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InventoryTransferAppService;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InventoryTransferRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryTransferRepository;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 同仓移储幂等与不变量测试
 *
 * @author hwyz_leo
 */
@ExtendWith(MockitoExtension.class)
class InventoryTransferAppServiceTest {

    @Mock
    private InventoryTransferRepository inventoryTransferRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private StorageHierarchyValidator storageHierarchyValidator;

    private InventoryTransferAppService service;

    @BeforeEach
    void setUp() {
        service = new InventoryTransferAppService(inventoryTransferRepository, inventoryRepository,
                storageHierarchyValidator);
    }

    private TransferInventoryCmd cmd() {
        return new TransferInventoryCmd("REQ1", "VIN1", "WH1", "A2", "L2", 3, "9");
    }

    private VehicleInventory inventory(InventoryState state) {
        return VehicleInventory.builder()
                .id(1L)
                .vin("VIN1")
                .buildConfigCode("CFG")
                .warehouseCode("WH1")
                .storageAreaCode("A1")
                .storageLocationCode("L1")
                .state(state)
                .rowVersion(3)
                .build();
    }

    @Test
    void shouldReturnOriginalRecordForRepeatedRequest() {
        InventoryTransferRecord record = InventoryTransferRecord.builder()
                .id(1L).requestId("REQ1").vin("VIN1").warehouseCode("WH1")
                .newStorageAreaCode("A2").newStorageLocationCode("L2").build();
        when(inventoryTransferRepository.findByRequestId("REQ1")).thenReturn(Optional.of(record));

        InventoryTransferRecord result = service.transfer(cmd());

        assertThat(result.getRequestId()).isEqualTo("REQ1");
        verify(inventoryRepository, never()).relocate(anyLong(), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void shouldRejectRequestReusedWithDifferentContent() {
        InventoryTransferRecord record = InventoryTransferRecord.builder()
                .id(1L).requestId("REQ1").vin("VIN1").warehouseCode("WH1")
                .newStorageAreaCode("A9").newStorageLocationCode("L9").build();
        when(inventoryTransferRepository.findByRequestId("REQ1")).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.transfer(cmd()))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.REQUEST_ID_CONFLICT.getCode());
    }

    @Test
    void shouldRejectTransferWhenVinNotAvailable() {
        when(inventoryTransferRepository.findByRequestId("REQ1")).thenReturn(Optional.empty());
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.of(inventory(InventoryState.RESERVED)));

        assertThatThrownBy(() -> service.transfer(cmd()))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.VIN_NOT_AVAILABLE.getCode());
        verify(inventoryTransferRepository, never()).insert(any(InventoryTransferRecord.class));
    }

    @Test
    void shouldRejectTransferWhenTargetLocationOccupied() {
        when(inventoryTransferRepository.findByRequestId("REQ1")).thenReturn(Optional.empty());
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.of(inventory(InventoryState.AVAILABLE)));
        when(inventoryRepository.isLocationOccupied("WH1", "L2")).thenReturn(true);

        assertThatThrownBy(() -> service.transfer(cmd()))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.STORAGE_LOCATION_OCCUPIED.getCode());
    }

    @Test
    void shouldPersistServerAuthoritativeOldLocation() {
        when(inventoryTransferRepository.findByRequestId("REQ1")).thenReturn(Optional.empty());
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.of(inventory(InventoryState.AVAILABLE)));
        when(inventoryRepository.isLocationOccupied("WH1", "L2")).thenReturn(false);
        when(inventoryRepository.relocate(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(1);

        InventoryTransferRecord record = service.transfer(cmd());

        assertThat(record.getOldStorageAreaCode()).isEqualTo("A1");
        assertThat(record.getOldStorageLocationCode()).isEqualTo("L1");
        assertThat(record.getNewStorageLocationCode()).isEqualTo("L2");
        verify(inventoryTransferRepository).insert(any(InventoryTransferRecord.class));
    }

    @Test
    void shouldRejectStaleInventoryVersion() {
        when(inventoryTransferRepository.findByRequestId("REQ1")).thenReturn(Optional.empty());
        when(inventoryRepository.findByVin("VIN1")).thenReturn(Optional.of(inventory(InventoryState.AVAILABLE)));
        when(inventoryRepository.isLocationOccupied("WH1", "L2")).thenReturn(false);
        when(inventoryRepository.relocate(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(0);

        assertThatThrownBy(() -> service.transfer(cmd()))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.STALE_VERSION.getCode());
    }
}
