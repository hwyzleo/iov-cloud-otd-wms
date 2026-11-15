package net.hwyz.iov.cloud.otd.wms.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.wms.service.application.dto.cmd.TransferInventoryCmd;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InventoryTransferRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InventoryTransferCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryTransferRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.service.StorageHierarchyValidator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 移储应用服务
 * <p>
 * 原位置以服务端当前库存为权威，目标位置必须同仓、启用、未占用；移储事实与库存位置在同一事务内完成。
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTransferAppService {

    private final InventoryTransferRepository inventoryTransferRepository;
    private final InventoryRepository inventoryRepository;
    private final StorageHierarchyValidator storageHierarchyValidator;

    /**
     * 查询移储记录
     *
     * @param criteria 查询条件
     * @return 移储列表
     */
    public List<InventoryTransferRecord> search(InventoryTransferCriteria criteria) {
        return inventoryTransferRepository.search(criteria);
    }

    /**
     * 按主键查询移储记录
     *
     * @param id 主键
     * @return 移储记录
     */
    public InventoryTransferRecord getById(Long id) {
        return inventoryTransferRepository.findById(id).orElse(null);
    }

    /**
     * 同仓移储
     *
     * @param cmd 移储命令
     * @return 移储记录
     */
    @Transactional(rollbackFor = Exception.class)
    public InventoryTransferRecord transfer(TransferInventoryCmd cmd) {
        Optional<InventoryTransferRecord> recorded = inventoryTransferRepository.findByRequestId(cmd.requestId());
        if (recorded.isPresent()) {
            InventoryTransferRecord record = recorded.get();
            boolean equivalent = record.getVin().equals(cmd.vin())
                    && record.getWarehouseCode().equals(cmd.warehouseCode())
                    && record.getNewStorageAreaCode().equals(cmd.newStorageAreaCode())
                    && record.getNewStorageLocationCode().equals(cmd.newStorageLocationCode());
            if (!equivalent) {
                throw WmsBusinessException.of(WmsErrorCode.REQUEST_ID_CONFLICT,
                        "移储请求[" + cmd.requestId() + "]已被不同内容复用");
            }
            return record;
        }

        VehicleInventory inventory = inventoryRepository.findByVin(cmd.vin())
                .orElseThrow(() -> WmsBusinessException.of(WmsErrorCode.VEHICLE_NOT_IN_WAREHOUSE,
                        "仓库[" + cmd.warehouseCode() + "]不存在车辆[" + cmd.vin() + "]库存"));
        if (!inventory.getWarehouseCode().equals(cmd.warehouseCode())) {
            throw WmsBusinessException.of(WmsErrorCode.VEHICLE_NOT_IN_WAREHOUSE,
                    "车辆[" + cmd.vin() + "]不在仓库[" + cmd.warehouseCode() + "]");
        }
        if (inventory.getState() != InventoryState.AVAILABLE) {
            throw WmsBusinessException.of(WmsErrorCode.VIN_NOT_AVAILABLE,
                    "车辆[" + cmd.vin() + "]状态为[" + inventory.getState() + "]，不允许移储");
        }
        storageHierarchyValidator.requireEnabledLocation(cmd.warehouseCode(), cmd.newStorageAreaCode(),
                cmd.newStorageLocationCode());
        if (inventoryRepository.isLocationOccupied(cmd.warehouseCode(), cmd.newStorageLocationCode())) {
            throw WmsBusinessException.of(WmsErrorCode.STORAGE_LOCATION_OCCUPIED,
                    "仓库[" + cmd.warehouseCode() + "]储位[" + cmd.newStorageLocationCode() + "]已被占用");
        }

        int expectedVersion = cmd.expectedVersion() != null ? cmd.expectedVersion() : inventory.getRowVersion();
        int rows = inventoryRepository.relocate(inventory.getId(), cmd.newStorageAreaCode(),
                cmd.newStorageLocationCode(), expectedVersion, cmd.operator());
        if (rows == 0) {
            throw WmsBusinessException.of(WmsErrorCode.STALE_VERSION,
                    "车辆[" + cmd.vin() + "]移储发生并发冲突");
        }

        InventoryTransferRecord record = InventoryTransferRecord.builder()
                .requestId(cmd.requestId())
                .vin(inventory.getVin())
                .buildConfigCode(inventory.getBuildConfigCode())
                .warehouseCode(inventory.getWarehouseCode())
                .oldStorageAreaCode(inventory.getStorageAreaCode())
                .oldStorageLocationCode(inventory.getStorageLocationCode())
                .newStorageAreaCode(cmd.newStorageAreaCode())
                .newStorageLocationCode(cmd.newStorageLocationCode())
                .transferTime(new Date())
                .transferBy(cmd.operator())
                .build();
        try {
            inventoryTransferRepository.insert(record);
        } catch (DuplicateKeyException e) {
            throw WmsBusinessException.of(WmsErrorCode.REQUEST_ID_CONFLICT,
                    "移储请求[" + cmd.requestId() + "]已存在");
        }
        InventoryTransferRecord persisted = inventoryTransferRepository.findByRequestId(cmd.requestId())
                .orElse(record);
        log.info("车辆[{}]由储位[{}/{}]移储至[{}/{}]", cmd.vin(), inventory.getStorageAreaCode(),
                inventory.getStorageLocationCode(), cmd.newStorageAreaCode(), cmd.newStorageLocationCode());
        return persisted;
    }
}
