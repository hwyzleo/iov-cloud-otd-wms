package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.OutboundOrderCriteria;
import net.hwyz.iov.cloud.otd.wms.service.domain.repository.OutboundOrderRepository;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.converter.PersistenceConverter;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.OutboundOrderDetailMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper.OutboundOrderMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.OutboundOrderDetailPo;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.OutboundOrderPo;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 出库单仓储实现
 *
 * @author hwyz_leo
 */
@Repository
@RequiredArgsConstructor
public class OutboundOrderRepositoryImpl implements OutboundOrderRepository {

    private final OutboundOrderMapper outboundOrderMapper;
    private final OutboundOrderDetailMapper outboundOrderDetailMapper;

    @Override
    public Optional<OutboundOrder> findById(Long id) {
        OutboundOrderPo po = outboundOrderMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(PersistenceConverter.toDomain(po, selectDetailPos(po.getOrderNum())));
    }

    @Override
    public Optional<OutboundOrder> findByOrderNum(String orderNum) {
        OutboundOrderPo po = outboundOrderMapper.selectPoByOrderNum(orderNum);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(PersistenceConverter.toDomain(po, selectDetailPos(orderNum)));
    }

    @Override
    public Optional<OutboundOrder> findBySourceRequest(String sourceSystem, String requestId) {
        OutboundOrderPo po = outboundOrderMapper.selectPoBySourceRequest(sourceSystem, requestId);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(PersistenceConverter.toDomain(po, selectDetailPos(po.getOrderNum())));
    }

    @Override
    public Optional<OutboundOrder> findBySourceOrder(String sourceSystem, String sourceOrderNum) {
        OutboundOrderPo po = outboundOrderMapper.selectPoBySourceOrder(sourceSystem, sourceOrderNum);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(PersistenceConverter.toDomain(po, selectDetailPos(po.getOrderNum())));
    }

    @Override
    public void insert(OutboundOrder order) {
        OutboundOrderPo orderPo = PersistenceConverter.toPo(order);
        outboundOrderMapper.insert(orderPo);
        for (OutboundOrderDetail detail : order.getDetails()) {
            outboundOrderDetailMapper.insert(PersistenceConverter.toPo(detail));
        }
    }

    @Override
    public int updateOrder(OutboundOrder order, int expectedRowVersion, String operator) {
        return outboundOrderMapper.updateStateByVersion(order.getId(),
                order.getState() == null ? null : order.getState().name(),
                order.getAggregateVersion(), operator, expectedRowVersion);
    }

    @Override
    public int updateDetail(OutboundOrderDetail detail, int expectedRowVersion, String operator) {
        Date now = new Date();
        return outboundOrderDetailMapper.update(null, Wrappers.<OutboundOrderDetailPo>lambdaUpdate()
                .eq(OutboundOrderDetailPo::getId, detail.getId())
                .eq(OutboundOrderDetailPo::getRowVersion, expectedRowVersion)
                .set(OutboundOrderDetailPo::getState,
                        detail.getState() == null ? null : detail.getState().name())
                .set(OutboundOrderDetailPo::getLoadTime, detail.getLoadTime())
                .set(OutboundOrderDetailPo::getLoadBy, detail.getLoadBy())
                .set(OutboundOrderDetailPo::getOutboundTime, detail.getOutboundTime())
                .set(OutboundOrderDetailPo::getOutboundBy, detail.getOutboundBy())
                .set(OutboundOrderDetailPo::getReleaseTime, detail.getReleaseTime())
                .set(OutboundOrderDetailPo::getReleaseBy, detail.getReleaseBy())
                .set(OutboundOrderDetailPo::getModifyBy, operator)
                .set(OutboundOrderDetailPo::getModifyTime, now)
                .setSql("row_version = row_version + 1"));
    }

    @Override
    public List<OutboundOrderDetail> findDetails(String orderNum) {
        return selectDetailPos(orderNum).stream().map(PersistenceConverter::toDomain).toList();
    }

    @Override
    public List<OutboundOrder> search(OutboundOrderCriteria criteria) {
        List<OutboundOrderPo> poList = outboundOrderMapper.search(criteria.orderNum(), criteria.sourceSystem(),
                criteria.sourceOrderNum(), criteria.warehouseCode(), criteria.state(),
                criteria.beginTime(), criteria.endTime());
        return PageUtil.convert(poList, po -> PersistenceConverter.toDomain(po, null));
    }

    private List<OutboundOrderDetailPo> selectDetailPos(String orderNum) {
        return outboundOrderDetailMapper.selectList(Wrappers.<OutboundOrderDetailPo>lambdaQuery()
                .eq(OutboundOrderDetailPo::getOrderNum, orderNum)
                .orderByAsc(OutboundOrderDetailPo::getId));
    }
}
