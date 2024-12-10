package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.StorageAreaPo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 整车储区 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-12-10
 */
@Mapper
public interface StorageAreaDao extends BaseDao<StorageAreaPo, Long> {

    /**
     * 批量物理删除仓库储区信息
     *
     * @param warehouseCode 仓库代码
     * @param ids           仓库储区ID数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(String warehouseCode, Long[] ids);

}
