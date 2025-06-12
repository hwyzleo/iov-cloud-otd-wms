package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryPo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 库存 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-11
 */
@Mapper
public interface InventoryDao extends BaseDao<InventoryPo, Long> {

    /**
     * 批量物理删除库存
     *
     * @param ids 库存ID
     * @return 删除数量
     */
    int batchPhysicalDeletePo(Long[] ids);

}
