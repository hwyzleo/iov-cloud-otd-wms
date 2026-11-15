package net.hwyz.iov.cloud.otd.wms.service.domain;

import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 当前库存不变量测试
 *
 * @author hwyz_leo
 */
class VehicleInventoryTest {

    private VehicleInventory available() {
        return VehicleInventory.builder()
                .id(1L)
                .vin("VIN1")
                .warehouseCode("WH1")
                .storageAreaCode("A1")
                .storageLocationCode("L1")
                .state(InventoryState.AVAILABLE)
                .rowVersion(1)
                .build();
    }

    @Test
    void shouldReserveAndKeepLocation() {
        VehicleInventory inventory = available();
        inventory.reserve("OUT1");
        assertThat(inventory.getState()).isEqualTo(InventoryState.RESERVED);
        assertThat(inventory.getReservedOrderNum()).isEqualTo("OUT1");
        assertThat(inventory.getStorageLocationCode()).isEqualTo("L1");
    }

    @Test
    void shouldReleaseOnlyMatchingReservation() {
        VehicleInventory inventory = available();
        inventory.reserve("OUT1");
        assertThatThrownBy(() -> inventory.release("OUT2")).isInstanceOf(WmsBusinessException.class);
        inventory.release("OUT1");
        assertThat(inventory.getState()).isEqualTo(InventoryState.AVAILABLE);
        assertThat(inventory.getReservedOrderNum()).isNull();
    }

    @Test
    void shouldClearLocationOnOutbound() {
        VehicleInventory inventory = available();
        inventory.reserve("OUT1");
        inventory.markOutbound("OUT1", "9", new Date());
        assertThat(inventory.getState()).isEqualTo(InventoryState.OUTBOUND);
        assertThat(inventory.getStorageAreaCode()).isNull();
        assertThat(inventory.getStorageLocationCode()).isNull();
        assertThat(inventory.getReservedOrderNum()).isNull();
        assertThat(inventory.getOutboundBy()).isEqualTo("9");
    }

    @Test
    void shouldRejectRelocateWhenNotAvailable() {
        VehicleInventory inventory = available();
        inventory.reserve("OUT1");
        assertThatThrownBy(() -> inventory.relocate("A2", "L2")).isInstanceOf(WmsBusinessException.class);
    }

    @Test
    void shouldRejectDoubleReservation() {
        VehicleInventory inventory = available();
        inventory.reserve("OUT1");
        assertThatThrownBy(() -> inventory.reserve("OUT2")).isInstanceOf(WmsBusinessException.class);
    }
}
