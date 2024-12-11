package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.StorageLocationPo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 整车储位 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-12-11
 */
@Mapper
public interface StorageLocationDao extends BaseDao<StorageLocationPo, Long> {

    /**
     * 批量物理删除仓库储位信息
     *
     * @param warehouseCode   仓库代码
     * @param storageAreaCode 仓库储区代码
     * @param ids             仓库储区ID数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(String warehouseCode, String storageAreaCode, Long[] ids);

}
