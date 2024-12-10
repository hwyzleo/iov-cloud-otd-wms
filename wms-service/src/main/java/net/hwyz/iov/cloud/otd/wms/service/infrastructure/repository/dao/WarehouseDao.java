package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.WarehousePo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 整车仓库 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-12-10
 */
@Mapper
public interface WarehouseDao extends BaseDao<WarehousePo, Long> {

    /**
     * 通过仓库代码查询仓库信息
     *
     * @param code 仓库代码
     * @return 仓库信息
     */
    WarehousePo selectPoByCode(String code);

    /**
     * 批量物理删除仓库信息
     *
     * @param ids 仓库ID数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(Long[] ids);

}
