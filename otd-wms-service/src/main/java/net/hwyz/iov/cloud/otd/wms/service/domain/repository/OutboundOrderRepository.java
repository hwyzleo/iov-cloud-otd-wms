package net.hwyz.iov.cloud.otd.wms.service.domain.repository;

import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.OutboundOrder;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.OutboundOrderDetail;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.OutboundOrderCriteria;

import java.util.List;
import java.util.Optional;

/**
 * 出库单仓储（领域定义的持久化端口）
 *
 * @author hwyz_leo
 */
public interface OutboundOrderRepository {

    /**
     * 按主键查询（含明细）
     *
     * @param id 主键
     * @return 出库单
     */
    Optional<OutboundOrder> findById(Long id);

    /**
     * 按出库单号查询（含明细）
     *
     * @param orderNum 出库单号
     * @return 出库单
     */
    Optional<OutboundOrder> findByOrderNum(String orderNum);

    /**
     * 按来源请求标识查询（含明细）
     *
     * @param sourceSystem 来源系统
     * @param requestId    首次请求标识
     * @return 出库单
     */
    Optional<OutboundOrder> findBySourceRequest(String sourceSystem, String requestId);

    /**
     * 按来源单号查询（含明细）
     *
     * @param sourceSystem   来源系统
     * @param sourceOrderNum 来源单号
     * @return 出库单
     */
    Optional<OutboundOrder> findBySourceOrder(String sourceSystem, String sourceOrderNum);

    /**
     * 写入出库单及明细
     *
     * @param order 出库单聚合
     */
    void insert(OutboundOrder order);

    /**
     * 条件更新订单状态与聚合版本
     *
     * @param order              出库单聚合
     * @param expectedRowVersion 预期行版本
     * @param operator           修改者
     * @return 影响行数
     */
    int updateOrder(OutboundOrder order, int expectedRowVersion, String operator);

    /**
     * 条件更新明细状态
     *
     * @param detail             明细
     * @param expectedRowVersion 预期行版本
     * @param operator           修改者
     * @return 影响行数
     */
    int updateDetail(OutboundOrderDetail detail, int expectedRowVersion, String operator);

    /**
     * 查询订单明细
     *
     * @param orderNum 出库单号
     * @return 明细列表
     */
    List<OutboundOrderDetail> findDetails(String orderNum);

    /**
     * 查询
     *
     * @param criteria 查询条件
     * @return 出库单列表（不含明细）
     */
    List<OutboundOrder> search(OutboundOrderCriteria criteria);
}
