package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryCountPo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 盘点 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-12
 */
@Mapper
public interface InventoryCountDao extends BaseDao<InventoryCountPo, Long> {

    /**
     * 批量物理删除盘点
     *
     * @param ids 库存ID
     * @return 删除数量
     */
    int batchPhysicalDeletePo(Long[] ids);

}
