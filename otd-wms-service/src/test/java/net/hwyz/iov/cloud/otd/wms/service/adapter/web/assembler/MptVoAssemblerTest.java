package net.hwyz.iov.cloud.otd.wms.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.wms.api.contract.InboundOrderMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryMpt;
import net.hwyz.iov.cloud.otd.wms.api.contract.InventoryTransferMpt;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InboundOrderDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InventoryDto;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.InventoryTransferDto;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 应用 DTO → 管理后台 VO 转换测试
 * <p>
 * 重点覆盖管理后台页面依赖的字段：生产配置代码、仓库名称、时间字段与操作人。
 *
 * @author hwyz_leo
 */
class MptVoAssemblerTest {

    @Test
    void shouldMapInboundFactFieldsForMptDetail() {
        Date inboundTime = new Date();
        InboundOrderDto dto = new InboundOrderDto(1L, "IN1", "PIN1", "VIN1", "CFG1", "WH1", "上海前置库", "PDC",
                "A1", "L1", inboundTime, "9", new Date());

        InboundOrderMpt vo = MptVoAssembler.fromInbound(dto);

        assertThat(vo.getOrderNum()).isEqualTo("IN1");
        assertThat(vo.getPreOrderNum()).isEqualTo("PIN1");
        assertThat(vo.getBuildConfigCode()).isEqualTo("CFG1");
        assertThat(vo.getWarehouseName()).isEqualTo("上海前置库");
        assertThat(vo.getInboundTime()).isEqualTo(inboundTime);
        assertThat(vo.getInboundBy()).isEqualTo(9L);
    }

    @Test
    void shouldMapTransferRecordOperatorAndLocation() {
        Date transferTime = new Date();
        InventoryTransferDto dto = new InventoryTransferDto(1L, "REQ1", "VIN1", "CFG1", "WH1", "上海前置库", "PDC",
                "A1", "L1", "A2", "L2", transferTime, "10", new Date());

        InventoryTransferMpt vo = MptVoAssembler.fromTransfer(dto);

        assertThat(vo.getVin()).isEqualTo("VIN1");
        assertThat(vo.getOldStorageLocationCode()).isEqualTo("L1");
        assertThat(vo.getNewStorageLocationCode()).isEqualTo("L2");
        assertThat(vo.getTransferTime()).isEqualTo(transferTime);
        assertThat(vo.getTransferBy()).isEqualTo(10L);
        assertThat(vo.getTransferState()).isNull();
    }

    @Test
    void shouldKeepInventoryStateAndNameForMptList() {
        InventoryDto dto = new InventoryDto(1L, "VIN1", "CFG1", "WH1", "上海前置库", "PDC", "A1", "L1",
                1, "AVAILABLE", null, new Date(), "9", null, null, 2, new Date());

        InventoryMpt vo = MptVoAssembler.fromInventory(dto);

        assertThat(vo.getBuildConfigCode()).isEqualTo("CFG1");
        assertThat(vo.getWarehouseName()).isEqualTo("上海前置库");
        assertThat(vo.getState()).isEqualTo(1);
        assertThat(vo.getStateName()).isEqualTo("AVAILABLE");
        assertThat(vo.getRowVersion()).isEqualTo(2);
    }

    @Test
    void shouldTolerateNonNumericOperator() {
        InboundOrderDto dto = new InboundOrderDto(1L, "IN1", "PIN1", "VIN1", "CFG1", "WH1", "上海前置库", "PDC",
                "A1", "L1", new Date(), "SYSTEM", new Date());

        assertThat(MptVoAssembler.fromInbound(dto).getInboundBy()).isNull();
    }
}
