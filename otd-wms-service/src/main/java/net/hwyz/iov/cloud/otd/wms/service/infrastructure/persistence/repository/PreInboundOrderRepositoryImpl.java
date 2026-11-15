package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.PreInboundOrderCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.PreInboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter.PersistenceConverter;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.PreInboundOrderMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 预入库单仓储实现
 *
 * @author hwyz_leo
 */
@Repository
@RequiredArgsConstructor
public class PreInboundOrderRepositoryImpl implements PreInboundOrderRepository {

    private final PreInboundOrderMapper preInboundOrderMapper;

    @Override
    public Optional<PreInboundOrder> findById(Long id) {
        return Optional.ofNullable(PersistenceConverter.toDomain(preInboundOrderMapper.selectPoById(id)));
    }

    @Override
    public Optional<PreInboundOrder> findByOrderNum(String orderNum) {
        return Optional.ofNullable(PersistenceConverter.toDomain(preInboundOrderMapper.selectPoByOrderNum(orderNum)));
    }

    @Override
    public Optional<PreInboundOrder> findByVin(String vin) {
        return Optional.ofNullable(PersistenceConverter.toDomain(preInboundOrderMapper.selectPoByVin(vin)));
    }

    @Override
    public void insert(PreInboundOrder order) {
        preInboundOrderMapper.insertPo(PersistenceConverter.toPo(order));
    }

    @Override
    public int updateState(PreInboundOrder order, int expectedVersion, String transitFromState, String operator) {
        return preInboundOrderMapper.updateStateByVersion(PersistenceConverter.toPo(order), expectedVersion,
                transitFromState, operator);
    }

    @Override
    public List<PreInboundOrder> search(PreInboundOrderCriteria criteria) {
        List<net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.PreInboundOrderPo> poList =
                preInboundOrderMapper.search(criteria.orderNum(), criteria.vin(), criteria.warehouseLevel(),
                        criteria.warehouseCode(), criteria.state(), criteria.beginTime(), criteria.endTime());
        return PageUtil.convert(poList, PersistenceConverter::toDomain);
    }
}
