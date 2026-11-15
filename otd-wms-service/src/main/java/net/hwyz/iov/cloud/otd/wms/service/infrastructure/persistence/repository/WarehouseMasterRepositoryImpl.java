package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.StorageAreaInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.StorageLocationInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.WarehouseInfo;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.WarehouseMasterRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter.PersistenceConverter;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.InboundOrderMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.InventoryMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.InventoryTransferMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.OutboundOrderDetailMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.StorageAreaMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.StorageLocationMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.WarehouseMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.OutboundOrderDetailPo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 仓储主数据仓储实现
 * <p>
 * 提供业务动作的层级校验读取与主数据物理删除的引用保护判定。
 *
 * @author hwyz_leo
 */
@Repository
@RequiredArgsConstructor
public class WarehouseMasterRepositoryImpl implements WarehouseMasterRepository {

    private final WarehouseMapper warehouseMapper;
    private final StorageAreaMapper storageAreaMapper;
    private final StorageLocationMapper storageLocationMapper;
    private final InventoryMapper inventoryMapper;
    private final InboundOrderMapper inboundOrderMapper;
    private final InventoryTransferMapper inventoryTransferMapper;
    private final OutboundOrderDetailMapper outboundOrderDetailMapper;

    @Override
    public Optional<WarehouseInfo> findWarehouse(String code) {
        return Optional.ofNullable(PersistenceConverter.toDomain(warehouseMapper.selectPoByCode(code)));
    }

    @Override
    public Optional<StorageAreaInfo> findArea(String warehouseCode, String areaCode) {
        return Optional.ofNullable(PersistenceConverter.toDomain(
                storageAreaMapper.selectPoByWarehouseAndCode(warehouseCode, areaCode)));
    }

    @Override
    public Optional<StorageLocationInfo> findLocation(String warehouseCode, String areaCode, String locationCode) {
        return Optional.ofNullable(PersistenceConverter.toDomain(
                storageLocationMapper.selectPoByWarehouseAreaAndCode(warehouseCode, areaCode, locationCode)));
    }

    @Override
    public boolean isWarehouseReferenced(String warehouseCode) {
        return countReferences(warehouseCode, null, null) > 0;
    }

    @Override
    public boolean isAreaReferenced(String warehouseCode, String areaCode) {
        return countReferences(warehouseCode, areaCode, null) > 0;
    }

    @Override
    public boolean isLocationReferenced(String warehouseCode, String locationCode) {
        long outboundDetailCount = outboundOrderDetailMapper.selectCount(Wrappers.<OutboundOrderDetailPo>lambdaQuery()
                .eq(OutboundOrderDetailPo::getWarehouseCode, warehouseCode)
                .eq(OutboundOrderDetailPo::getStorageLocationCode, locationCode));
        return countReferences(warehouseCode, null, locationCode) > 0 || outboundDetailCount > 0;
    }

    private long countReferences(String warehouseCode, String areaCode, String locationCode) {
        return (long) inventoryMapper.countReferences(warehouseCode, areaCode, locationCode)
                + inboundOrderMapper.countReferences(warehouseCode, areaCode, locationCode)
                + inventoryTransferMapper.countReferences(warehouseCode, areaCode, locationCode);
    }
}
