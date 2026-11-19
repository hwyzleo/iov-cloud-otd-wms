package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.InboundTask;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InboundTaskCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.InboundTaskRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter.PersistenceConverter;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.InboundTaskMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InboundTaskPo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 交付库待入库任务仓储实现
 *
 * @author hwyz_leo
 */
@Repository
@RequiredArgsConstructor
public class InboundTaskRepositoryImpl implements InboundTaskRepository {

    private final InboundTaskMapper inboundTaskMapper;

    @Override
    public Optional<InboundTask> findById(Long id) {
        return Optional.ofNullable(PersistenceConverter.toDomain(inboundTaskMapper.selectPoById(id)));
    }

    @Override
    public Optional<InboundTask> findByOrderNum(String orderNum) {
        return Optional.ofNullable(PersistenceConverter.toDomain(inboundTaskMapper.selectPoByOrderNum(orderNum)));
    }

    @Override
    public Optional<InboundTask> findBySourceRequest(String sourceSystem, String requestId) {
        return Optional.ofNullable(PersistenceConverter.toDomain(
                inboundTaskMapper.selectPoBySourceRequest(sourceSystem, requestId)));
    }

    @Override
    public Optional<InboundTask> findBySourceOrderAndVin(String sourceSystem, String sourceOrderNum, String vin) {
        return Optional.ofNullable(PersistenceConverter.toDomain(
                inboundTaskMapper.selectPoBySourceOrderAndVin(sourceSystem, sourceOrderNum, vin)));
    }

    @Override
    public void insert(InboundTask task) {
        inboundTaskMapper.insertPo(PersistenceConverter.toPo(task));
    }

    @Override
    public int update(InboundTask task, int expectedVersion, String operator) {
        return inboundTaskMapper.updateStateByVersion(PersistenceConverter.toPo(task), expectedVersion, operator);
    }

    @Override
    public List<InboundTask> search(InboundTaskCriteria criteria) {
        List<InboundTaskPo> poList = inboundTaskMapper.search(criteria.orderNum(), criteria.sourceSystem(),
                criteria.sourceOrderNum(), criteria.warehouseCode(), criteria.state(), criteria.vin(),
                criteria.beginTime(), criteria.endTime());
        return PageUtil.convert(poList, PersistenceConverter::toDomain);
    }
}
