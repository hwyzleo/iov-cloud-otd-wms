package net.hwyz.iov.cloud.otd.wms.service.domain.repository;

import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.PreInboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.PreInboundOrderCriteria;

import java.util.List;
import java.util.Optional;

/**
 * 预入库单仓储（领域定义的持久化端口）
 *
 * @author hwyz_leo
 */
public interface PreInboundOrderRepository {

    /**
     * 按主键查询
     *
     * @param id 主键
     * @return 预入库单
     */
    Optional<PreInboundOrder> findById(Long id);

    /**
     * 按单号查询
     *
     * @param orderNum 预入库单号
     * @return 预入库单
     */
    Optional<PreInboundOrder> findByOrderNum(String orderNum);

    /**
     * 按 VIN 查询（当前生命周期内 VIN 唯一）
     *
     * @param vin 车架号
     * @return 预入库单
     */
    Optional<PreInboundOrder> findByVin(String vin);

    /**
     * 插入待到达预入库单
     *
     * @param order 预入库单
     */
    void insert(PreInboundOrder order);

    /**
     * 条件更新状态（按行版本 CAS，含 transitFromState 前置态校验）
     *
     * @param order            最新预入库单聚合
     * @param expectedVersion  预期行版本
     * @param transitFromState 允许的前置状态名
     * @param operator         修改者
     * @return 影响行数（0 表示并发冲突或状态不符）
     */
    int updateState(PreInboundOrder order, int expectedVersion, String transitFromState, String operator);

    /**
     * 查询
     *
     * @param criteria 查询条件
     * @return 预入库单列表
     */
    List<PreInboundOrder> search(PreInboundOrderCriteria criteria);
}
