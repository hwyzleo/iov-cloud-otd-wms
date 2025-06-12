package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryCountDetailPo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 盘点明细 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-12
 */
@Mapper
public interface InventoryCountDetailDao extends BaseDao<InventoryCountDetailPo, Long> {

    /**
     * 根据盘点单号和明细ID查询盘点明细
     *
     * @param orderNum 盘点单号
     * @param id       明细ID
     * @return 盘点明细
     */
    InventoryCountDetailPo selectPoByOrderNumAndId(String orderNum, Long id);

    /**
     * 批量物理删除盘点明细
     *
     * @param orderNum 盘点单号
     * @param ids      库存ID
     * @return 删除数量
     */
    int batchPhysicalDeletePo(String orderNum, Long[] ids);

}
