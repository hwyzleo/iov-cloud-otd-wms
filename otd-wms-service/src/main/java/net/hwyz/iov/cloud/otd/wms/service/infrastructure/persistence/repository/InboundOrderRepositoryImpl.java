package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboundOrderFact;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InboundOrderCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter.PersistenceConverter;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.InboundOrderMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InboundOrderPo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 入库单仓储实现
 *
 * @author hwyz_leo
 */
@Repository
@RequiredArgsConstructor
public class InboundOrderRepositoryImpl implements InboundOrderRepository {

    private final InboundOrderMapper inboundOrderMapper;

    @Override
    public Optional<InboundOrderFact> findById(Long id) {
        return Optional.ofNullable(PersistenceConverter.toDomain(inboundOrderMapper.selectPoById(id)));
    }

    @Override
    public Optional<InboundOrderFact> findByPreOrderNum(String preOrderNum) {
        return Optional.ofNullable(PersistenceConverter.toDomain(inboundOrderMapper.selectPoByPreOrderNum(preOrderNum)));
    }

    @Override
    public void insert(InboundOrderFact fact) {
        inboundOrderMapper.insertPo(PersistenceConverter.toPo(fact));
    }

    @Override
    public List<InboundOrderFact> search(InboundOrderCriteria criteria) {
        List<InboundOrderPo> poList = inboundOrderMapper.search(criteria.orderNum(), criteria.preOrderNum(),
                criteria.vin(), criteria.warehouseLevel(), criteria.warehouseCode(),
                criteria.beginTime(), criteria.endTime());
        return PageUtil.convert(poList, PersistenceConverter::toDomain);
    }
}
