package net.hwyz.iov.cloud.otd.wms.service.integration;

import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CancelOutboundOrderCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmArrivalCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConfirmInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.ConsumeVehicleProducedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.CreatePreInboundCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.LoadVinCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.OutboundVinCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.TransferInventoryCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.VehicleOutboundRequestedCmd;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.result.ConsumeResultDto;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.InventoryTransferAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.OutboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.application.service.PreInboundOrderAppService;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.ConsumeOutcome;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundDetailState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.OutboundOrderState;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.PreInboundState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InventoryTransferRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.PreInboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.StorageAreaMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.StorageLocationMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.WarehouseMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageAreaPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageLocationPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WarehousePo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 基础闭环集成测试（真实 MySQL 约束与事务）
 *
 * @author hwyz_leo
 */
class WmsBasicVehicleLoopIntegrationTest extends AbstractWmsIntegrationTest {

    private static final String WAREHOUSE = "WH-DEFAULT";
    private static final String AREA = "A1";

    @Autowired
    private PreInboundOrderAppService preInboundOrderAppService;
    @Autowired
    private InboundOrderAppService inboundOrderAppService;
    @Autowired
    private OutboundOrderAppService outboundOrderAppService;
    @Autowired
    private InventoryTransferAppService inventoryTransferAppService;
    @Autowired
    private PreInboundOrderRepository preInboundOrderRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private OutboundOrderRepository outboundOrderRepository;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private StorageAreaMapper storageAreaMapper;
    @Autowired
    private StorageLocationMapper storageLocationMapper;

    // ==================== 入库闭环 ====================

    @Test
    void shouldConsumeVmdFactAndCompleteInboundIdempotently() {
        prepareMasterData();
        String vin = "VIN-VMD-1";

        ConsumeResultDto consumed = preInboundOrderAppService.consumeVehicleProduced(
                new ConsumeVehicleProducedCmd("EVT-" + UUID.randomUUID(), "vmd.vehicle-produce", "BATCH-1", vin,
                        "CFG-VMD", null, "digest"));
        assertThat(consumed.outcome()).isEqualTo(ConsumeOutcome.PROCESSED);

        PreInboundOrder order = preInboundOrderRepository.findByVin(vin).orElseThrow();
        assertThat(order.getState()).isEqualTo(PreInboundState.PENDING_ARRIVAL);
        assertThat(order.getBuildConfigCode()).isEqualTo("CFG-VMD");
        assertThat(order.getWarehouseCode()).isEqualTo(WAREHOUSE);
        assertThat(order.getSourceSystem()).isEqualTo("VMD");
        assertThat(inventoryRepository.findByVin(vin)).isEmpty();

        inboundOrderAppService.confirmArrival(new ConfirmArrivalCmd(order.getOrderNum(), "REQ-A", null, "9"));
        inboundOrderAppService.confirmInbound(new ConfirmInboundCmd(order.getOrderNum(), "REQ-I", AREA, "L1",
                null, "9"));

        VehicleInventory inventory = inventoryRepository.findByVin(vin).orElseThrow();
        assertThat(inventory.getState()).isEqualTo(InventoryState.AVAILABLE);
        assertThat(inventory.getStorageLocationCode()).isEqualTo("L1");
        assertThat(count("tb_inbound_order")).isEqualTo(1);

        PreInboundOrder replayed = inboundOrderAppService.confirmInbound(new ConfirmInboundCmd(
                order.getOrderNum(), "REQ-I-2", AREA, "L2", null, "9"));
        assertThat(replayed.getState()).isEqualTo(PreInboundState.COMPLETED);
        assertThat(count("tb_inbound_order")).isEqualTo(1);
        assertThat(inventoryRepository.findByVin(vin).orElseThrow().getStorageLocationCode()).isEqualTo("L1");
    }

    @Test
    void shouldRejectInboundToOccupiedLocation() {
        prepareMasterData();
        String first = prepareAvailableVin("VIN-1", "L1");
        String second = createArrivedPreInbound("VIN-2");

        assertThatThrownBy(() -> inboundOrderAppService.confirmInbound(new ConfirmInboundCmd(second, "REQ-2",
                AREA, "L1", null, "9")))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.STORAGE_LOCATION_OCCUPIED.getCode());

        assertThat(inventoryRepository.findByVin(first)).isPresent();
        assertThat(inventoryRepository.findByVin("VIN-2")).isEmpty();
        assertThat(preInboundOrderRepository.findByVin("VIN-2").orElseThrow().getState())
                .isEqualTo(PreInboundState.ARRIVED);
    }

    // ==================== 出库闭环 ====================

    @Test
    void shouldReserveLoadAndOutboundPerVin() {
        prepareMasterData();
        String vin1 = prepareAvailableVin("VIN-1", "L1");
        String vin2 = prepareAvailableVin("VIN-2", "L2");

        ConsumeResultDto reserved = outboundOrderAppService.consumeRequested(command("REQ-1", "SRC-1",
                List.of(vin1, vin2)));
        assertThat(reserved.outcome()).isEqualTo(ConsumeOutcome.PROCESSED);

        OutboundOrder order = outboundOrderRepository.findBySourceOrder("TMS", "SRC-1").orElseThrow();
        assertThat(order.getState()).isEqualTo(OutboundOrderState.RESERVED);
        assertThat(order.getAggregateVersion()).isEqualTo(1L);
        assertThat(inventoryRepository.findByVin(vin1).orElseThrow().getState())
                .isEqualTo(InventoryState.RESERVED);
        assertThat(inventoryRepository.findByVin(vin1).orElseThrow().getReservedOrderNum())
                .isEqualTo(order.getOrderNum());
        assertThat(count("tb_wms_outbox")).isEqualTo(1);

        outboundOrderAppService.loadVin(new LoadVinCmd(order.getOrderNum(), vin1, "LOAD-1", null, "9"));
        OutboundOrder loaded = outboundOrderRepository.findByOrderNum(order.getOrderNum()).orElseThrow();
        assertThat(loaded.getState()).isEqualTo(OutboundOrderState.PARTIALLY_LOADED);
        assertThat(loaded.getDetails()).filteredOn(d -> d.getVin().equals(vin1)).first()
                .extracting(d -> d.getState()).isEqualTo(OutboundDetailState.LOADED);
        assertThat(inventoryRepository.findByVin(vin1).orElseThrow().getStorageLocationCode()).isEqualTo("L1");
        assertThat(count("tb_wms_outbox")).isEqualTo(2);

        // 相同 requestId 重复调用保持幂等
        outboundOrderAppService.loadVin(new LoadVinCmd(order.getOrderNum(), vin1, "LOAD-1", null, "9"));
        assertThat(count("tb_wms_outbox")).isEqualTo(2);

        outboundOrderAppService.outboundVin(new OutboundVinCmd(order.getOrderNum(), vin1, "OUT-1", null, "9"));
        VehicleInventory outboundInventory = inventoryRepository.findByVin(vin1).orElseThrow();
        assertThat(outboundInventory.getState()).isEqualTo(InventoryState.OUTBOUND);
        assertThat(outboundInventory.getStorageAreaCode()).isNull();
        assertThat(outboundInventory.getStorageLocationCode()).isNull();
        assertThat(outboundInventory.getReservedOrderNum()).isNull();

        assertThatThrownBy(() -> outboundOrderAppService.cancel(new CancelOutboundOrderCmd(
                order.getOrderNum(), "CANCEL-1", "late", null, "9")))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.OUTBOUND_STATE_CONFLICT.getCode());

        outboundOrderAppService.loadVin(new LoadVinCmd(order.getOrderNum(), vin2, "LOAD-2", null, "9"));
        outboundOrderAppService.outboundVin(new OutboundVinCmd(order.getOrderNum(), vin2, "OUT-2", null, "9"));
        assertThat(outboundOrderRepository.findByOrderNum(order.getOrderNum()).orElseThrow().getState())
                .isEqualTo(OutboundOrderState.COMPLETED);
    }

    @Test
    void shouldRejectWholeBatchWithoutOrderOrReservation() {
        prepareMasterData();
        String vin1 = prepareAvailableVin("VIN-1", "L1");

        ConsumeResultDto rejected = outboundOrderAppService.consumeRequested(command("REQ-R", "SRC-R",
                List.of(vin1, "VIN-MISSING")));

        assertThat(rejected.outcome()).isEqualTo(ConsumeOutcome.BUSINESS_REJECTED);
        assertThat(count("tb_outbound_order")).isZero();
        assertThat(count("tb_outbound_order_detail")).isZero();
        assertThat(inventoryRepository.findByVin(vin1).orElseThrow().getState())
                .isEqualTo(InventoryState.AVAILABLE);
        assertThat(count("tb_wms_outbox")).isEqualTo(1);
        String eventType = jdbcTemplate.queryForObject("SELECT event_type FROM tb_wms_outbox", String.class);
        assertThat(eventType).isEqualTo("VehicleOutboundCommandRejectedEvent");
        String reason = jdbcTemplate.queryForObject(
                "SELECT payload FROM tb_wms_outbox", String.class);
        assertThat(reason).contains("302010").contains("VIN-MISSING");
    }

    @Test
    void shouldAllowOnlyOneConcurrentReservationOfSameVin() throws Exception {
        prepareMasterData();
        String vin = prepareAvailableVin("VIN-C", "L1");

        List<Callable<ConsumeResultDto>> tasks = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String suffix = "C" + i;
            tasks.add(() -> outboundOrderAppService.consumeRequested(command("REQ-" + suffix, "SRC-" + suffix,
                    List.of(vin))));
        }
        List<Object> outcomes = runConcurrently(tasks);

        long processed = outcomes.stream().filter(o -> o instanceof ConsumeResultDto r
                && r.outcome() == ConsumeOutcome.PROCESSED).count();
        assertThat(outcomes).hasSize(2);
        assertThat(processed).isEqualTo(1);
        assertThat(count("tb_outbound_order")).isEqualTo(1);
        assertThat(inventoryRepository.findByVin(vin).orElseThrow().getState())
                .isEqualTo(InventoryState.RESERVED);
    }

    @Test
    void shouldRollbackOutboxTogetherWithBusinessFailure() {
        prepareMasterData();
        String vin = prepareAvailableVin("VIN-1", "L1");
        outboundOrderAppService.consumeRequested(command("REQ-1", "SRC-1", List.of(vin)));
        OutboundOrder order = outboundOrderRepository.findBySourceOrder("TMS", "SRC-1").orElseThrow();
        long outboxBefore = count("tb_wms_outbox");

        assertThatThrownBy(() -> outboundOrderAppService.loadVin(new LoadVinCmd(order.getOrderNum(), vin,
                "LOAD-X", 99, "9")))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.STALE_VERSION.getCode());

        assertThat(count("tb_wms_outbox")).isEqualTo(outboxBefore);
        assertThat(outboundOrderRepository.findByOrderNum(order.getOrderNum()).orElseThrow().getState())
                .isEqualTo(OutboundOrderState.RESERVED);
    }

    // ==================== 移储 ====================

    @Test
    void shouldTransferIdempotentlyAndRejectConcurrentTargetConflict() throws Exception {
        prepareMasterData();
        prepareLocation("L2");
        prepareLocation("L5");
        String vin1 = prepareAvailableVin("VIN-1", "L1");
        String vin2 = prepareAvailableVin("VIN-2", "L5-TMP");

        InventoryTransferRecord first = inventoryTransferAppService.transfer(new TransferInventoryCmd("T-1", vin1,
                WAREHOUSE, AREA, "L2", null, "9"));
        assertThat(first.getOldStorageLocationCode()).isEqualTo("L1");
        assertThat(first.getNewStorageLocationCode()).isEqualTo("L2");
        assertThat(inventoryRepository.findByVin(vin1).orElseThrow().getStorageLocationCode()).isEqualTo("L2");

        InventoryTransferRecord replayed = inventoryTransferAppService.transfer(new TransferInventoryCmd("T-1",
                vin1, WAREHOUSE, AREA, "L2", null, "9"));
        assertThat(replayed.getId()).isEqualTo(first.getId());
        assertThat(count("tb_inventory_transfer")).isEqualTo(1);

        assertThatThrownBy(() -> inventoryTransferAppService.transfer(new TransferInventoryCmd("T-1", vin1,
                WAREHOUSE, AREA, "L5", null, "9")))
                .isInstanceOf(WmsBusinessException.class)
                .extracting(e -> ((WmsBusinessException) e).getErrorCode().getCode())
                .isEqualTo(WmsErrorCode.REQUEST_ID_CONFLICT.getCode());

        List<Callable<Object>> tasks = new ArrayList<>();
        int index = 0;
        for (String vin : List.of(vin1, vin2)) {
            String requestId = "T-C" + index++;
            tasks.add(() -> inventoryTransferAppService.transfer(new TransferInventoryCmd(requestId, vin,
                    WAREHOUSE, AREA, "L5", null, "9")));
        }
        List<Object> outcomes = runConcurrently(tasks);
        long succeeded = outcomes.stream().filter(o -> o instanceof InventoryTransferRecord).count();
        assertThat(succeeded).isEqualTo(1);
        // 先前幂等移储 1 条 + 并发中成功 1 条
        assertThat(count("tb_inventory_transfer")).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_inventory WHERE storage_location_code = 'L5'", Long.class)).isEqualTo(1);
    }

    // ==================== 主数据层级唯一 ====================

    @Test
    void shouldRejectDuplicateMasterDataCodesWithinHierarchy() {
        prepareMasterData();

        assertThatThrownBy(() -> storageAreaMapper.insertPo(StorageAreaPo.builder()
                .code(AREA).name("重复储区").warehouseCode(WAREHOUSE).enable(true).sort(1).build()))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);

        assertThatThrownBy(() -> storageLocationMapper.insertPo(StorageLocationPo.builder()
                .code("L1").name("重复储位").warehouseCode(WAREHOUSE).storageAreaCode(AREA)
                .enable(true).sort(1).build()))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);

        // 相同储位代码在不同储区下允许存在
        storageAreaMapper.insertPo(StorageAreaPo.builder()
                .code("A2").name("A2 区").warehouseCode(WAREHOUSE).enable(true).sort(1).build());
        storageLocationMapper.insertPo(StorageLocationPo.builder()
                .code("L1").name("L1").warehouseCode(WAREHOUSE).storageAreaCode("A2").enable(true).sort(1).build());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_storage_location WHERE code = 'L1'", Long.class)).isEqualTo(2);
    }

    // ==================== helpers ====================

    private List<Object> runConcurrently(List<? extends Callable<?>> tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Callable<?> task : tasks) {
                futures.add(executor.submit(() -> {
                    start.await(5, TimeUnit.SECONDS);
                    try {
                        return (Object) task.call();
                    } catch (Exception e) {
                        return e;
                    }
                }));
            }
            start.countDown();
            List<Object> results = new ArrayList<>();
            for (Future<Object> future : futures) {
                results.add(future.get(30, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private VehicleOutboundRequestedCmd command(String requestId, String sourceOrderNum, List<String> vins) {
        return new VehicleOutboundRequestedCmd("EVT-" + UUID.randomUUID(), "wms.vehicle-outbound.requested",
                "digest-" + requestId, requestId, "TMS", sourceOrderNum, null, vins, "0");
    }

    private void prepareMasterData() {
        if (warehouseMapper.selectPoByCode(WAREHOUSE) == null) {
            warehouseMapper.insertPo(WarehousePo.builder()
                    .code(WAREHOUSE).name("默认仓库").warehouseLevel("PDC").enable(true).sort(1).build());
            storageAreaMapper.insertPo(StorageAreaPo.builder()
                    .code(AREA).name("A1 区").warehouseCode(WAREHOUSE).enable(true).sort(1).build());
        }
        for (String location : List.of("L1", "L2", "L3", "L4", "L5", "L5-TMP")) {
            prepareLocation(location);
        }
    }

    private void prepareLocation(String locationCode) {
        if (storageLocationMapper.selectPoByWarehouseAreaAndCode(WAREHOUSE, AREA, locationCode) == null) {
            storageLocationMapper.insertPo(StorageLocationPo.builder()
                    .code(locationCode).name(locationCode).warehouseCode(WAREHOUSE).storageAreaCode(AREA)
                    .enable(true).sort(1).build());
        }
    }

    private String prepareAvailableVin(String vin, String locationCode) {
        String orderNum = createArrivedPreInbound(vin);
        inboundOrderAppService.confirmInbound(new ConfirmInboundCmd(orderNum, "REQ-" + vin, AREA, locationCode,
                null, "9"));
        return vin;
    }

    private String createArrivedPreInbound(String vin) {
        PreInboundOrder order = preInboundOrderAppService.createPreInbound(new CreatePreInboundCmd(vin,
                "CFG-" + vin, null, "PDC", null, null, "9"));
        inboundOrderAppService.confirmArrival(new ConfirmArrivalCmd(order.getOrderNum(), "ARR-" + vin, null, "9"));
        return order.getOrderNum();
    }

    private long count(String table) {
        Long value = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
        return value == null ? 0 : value;
    }
}
