package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryTransferPo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 移库 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-12
 */
@Mapper
public interface InventoryTransferDao extends BaseDao<InventoryTransferPo, Long> {

    /**
     * 批量物理删除移库信息
     *
     * @param ids 移库信息ID
     * @return 删除数量
     */
    int batchPhysicalDeletePo(Long[] ids);

}
