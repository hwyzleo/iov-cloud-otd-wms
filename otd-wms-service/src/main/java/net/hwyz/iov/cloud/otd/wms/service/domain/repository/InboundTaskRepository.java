package net.hwyz.iov.cloud.otd.wms.service.domain.repository;

import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.InboundTask;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InboundTaskCriteria;

import java.util.List;
import java.util.Optional;

/**
 * 交付库待入库任务仓储（领域定义的持久化端口）
 *
 * @author hwyz_leo
 */
public interface InboundTaskRepository {

    /**
     * 按主键查询
     *
     * @param id 主键
     * @return 待入库任务
     */
    Optional<InboundTask> findById(Long id);

    /**
     * 按任务号查询
     *
     * @param orderNum 任务号
     * @return 待入库任务
     */
    Optional<InboundTask> findByOrderNum(String orderNum);

    /**
     * 按来源请求标识查询
     *
     * @param sourceSystem 来源系统
     * @param requestId    请求标识
     * @return 待入库任务
     */
    Optional<InboundTask> findBySourceRequest(String sourceSystem, String requestId);

    /**
     * 按来源单号与 VIN 查询
     *
     * @param sourceSystem   来源系统
     * @param sourceOrderNum 来源单号
     * @param vin            车架号
     * @return 待入库任务
     */
    Optional<InboundTask> findBySourceOrderAndVin(String sourceSystem, String sourceOrderNum, String vin);

    /**
     * 写入待入库任务
     *
     * @param task 待入库任务
     */
    void insert(InboundTask task);

    /**
     * 条件更新任务状态与聚合版本
     *
     * @param task            待入库任务
     * @param expectedVersion 预期行版本
     * @param operator        可信操作人
     * @return 影响行数
     */
    int update(InboundTask task, int expectedVersion, String operator);

    /**
     * 查询
     *
     * @param criteria 查询条件
     * @return 任务列表
     */
    List<InboundTask> search(InboundTaskCriteria criteria);
}
