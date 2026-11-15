package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryCountDetailPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 盘点明细 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface InventoryCountDetailMapper extends BaseDao<InventoryCountDetailPo, Long> {

    /**
     * 根据盘点单号和明细ID查询盘点明细
     *
     * @param orderNum 盘点单号
     * @param id       明细ID
     * @return 盘点明细
     */
    InventoryCountDetailPo selectPoByOrderNumAndId(@Param("orderNum") String orderNum, @Param("id") Long id);

    /**
     * 批量物理删除盘点明细（仅删除 tb_inventory_count_detail，不影响盘点主单）
     *
     * @param orderNum 盘点单号
     * @param ids      明细ID
     * @return 删除数量
     */
    int batchPhysicalDeletePo(@Param("orderNum") String orderNum, @Param("ids") Long[] ids);

}
