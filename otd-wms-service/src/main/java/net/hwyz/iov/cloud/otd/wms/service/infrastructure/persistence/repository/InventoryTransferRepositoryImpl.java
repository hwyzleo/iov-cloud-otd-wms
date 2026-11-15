package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InventoryTransferRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InventoryTransferCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InventoryTransferRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter.PersistenceConverter;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.InventoryTransferMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryTransferPo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 移储仓储实现
 *
 * @author hwyz_leo
 */
@Repository
@RequiredArgsConstructor
public class InventoryTransferRepositoryImpl implements InventoryTransferRepository {

    private final InventoryTransferMapper inventoryTransferMapper;

    @Override
    public Optional<InventoryTransferRecord> findByRequestId(String requestId) {
        return Optional.ofNullable(PersistenceConverter.toDomain(inventoryTransferMapper.selectPoByRequestId(requestId)));
    }

    @Override
    public Optional<InventoryTransferRecord> findById(Long id) {
        return Optional.ofNullable(PersistenceConverter.toDomain(inventoryTransferMapper.selectPoById(id)));
    }

    @Override
    public void insert(InventoryTransferRecord record) {
        inventoryTransferMapper.insertPo(PersistenceConverter.toPo(record));
    }

    @Override
    public List<InventoryTransferRecord> search(InventoryTransferCriteria criteria) {
        List<InventoryTransferPo> poList = inventoryTransferMapper.search(criteria.vin(), criteria.warehouseLevel(),
                criteria.warehouseCode(), criteria.beginTime(), criteria.endTime());
        return PageUtil.convert(poList, PersistenceConverter::toDomain);
    }
}
