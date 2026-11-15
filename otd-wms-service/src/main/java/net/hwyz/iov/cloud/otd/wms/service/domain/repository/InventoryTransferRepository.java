package net.hwyz.iov.cloud.otd.wms.service.domain.repository;

import net.hwyz.iov.cloud.otd.wms.service.domain.model.entity.InventoryTransferRecord;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InventoryTransferCriteria;

import java.util.List;
import java.util.Optional;

/**
 * 移储仓储（领域定义的持久化端口）
 *
 * @author hwyz_leo
 */
public interface InventoryTransferRepository {

    /**
     * 按请求标识查询（幂等）
     *
     * @param requestId 请求标识
     * @return 移储记录
     */
    Optional<InventoryTransferRecord> findByRequestId(String requestId);

    /**
     * 按主键查询
     *
     * @param id 主键
     * @return 移储记录
     */
    Optional<InventoryTransferRecord> findById(Long id);

    /**
     * 写入不可变移储事实
     *
     * @param record 移储记录
     */
    void insert(InventoryTransferRecord record);

    /**
     * 查询
     *
     * @param criteria 查询条件
     * @return 移储列表
     */
    List<InventoryTransferRecord> search(InventoryTransferCriteria criteria);
}
