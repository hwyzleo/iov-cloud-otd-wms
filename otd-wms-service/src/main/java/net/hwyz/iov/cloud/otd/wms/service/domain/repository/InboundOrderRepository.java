package net.hwyz.iov.cloud.otd.wms.service.domain.repository;

import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InboundOrderFact;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InboundOrderCriteria;

import java.util.List;
import java.util.Optional;

/**
 * 入库单仓储（领域定义的持久化端口）
 *
 * @author hwyz_leo
 */
public interface InboundOrderRepository {

    /**
     * 按主键查询
     *
     * @param id 主键
     * @return 入库单事实
     */
    Optional<InboundOrderFact> findById(Long id);

    /**
     * 按预入库单号查询
     *
     * @param preOrderNum 预入库单号
     * @return 入库单事实
     */
    Optional<InboundOrderFact> findByPreOrderNum(String preOrderNum);

    /**
     * 写入不可变入库事实
     *
     * @param fact 入库单事实
     */
    void insert(InboundOrderFact fact);

    /**
     * 查询
     *
     * @param criteria 查询条件
     * @return 入库单列表
     */
    List<InboundOrderFact> search(InboundOrderCriteria criteria);
}
