package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InventoryCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter.PersistenceConverter;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.InventoryMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryPo;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 当前库存仓储实现
 *
 * @author hwyz_leo
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryMapper inventoryMapper;

    @Override
    public Optional<VehicleInventory> findById(Long id) {
        return Optional.ofNullable(PersistenceConverter.toDomain(inventoryMapper.selectPoById(id)));
    }

    @Override
    public Optional<VehicleInventory> findByVin(String vin) {
        return Optional.ofNullable(PersistenceConverter.toDomain(inventoryMapper.selectPoByVin(vin)));
    }

    @Override
    public void insert(VehicleInventory inventory) {
        inventoryMapper.insertPo(PersistenceConverter.toPo(inventory));
    }

    @Override
    public int reserve(String vin, String orderNum) {
        return inventoryMapper.reserveByVin(vin, orderNum);
    }

    @Override
    public int release(String vin, String orderNum) {
        return inventoryMapper.releaseByVin(vin, orderNum);
    }

    @Override
    public int markOutbound(String vin, String orderNum, String operator, Date now) {
        return inventoryMapper.outboundByVin(vin, orderNum, operator, now);
    }

    @Override
    public int relocate(Long id, String newAreaCode, String newLocationCode, int expectedVersion, String operator) {
        return inventoryMapper.relocateById(id, newAreaCode, newLocationCode, expectedVersion, operator);
    }

    @Override
    public boolean isLocationOccupied(String warehouseCode, String storageLocationCode) {
        return inventoryMapper.countOccupiedLocation(warehouseCode, storageLocationCode) > 0;
    }

    @Override
    public List<VehicleInventory> search(InventoryCriteria criteria) {
        List<InventoryPo> poList = inventoryMapper.search(criteria.vin(), criteria.warehouseLevel(),
                criteria.warehouseCode(), criteria.state(), criteria.storageAreaCode(),
                criteria.storageLocationCode(), criteria.beginTime(), criteria.endTime());
        return PageUtil.convert(poList, PersistenceConverter::toDomain);
    }
}
