package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageLocationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 整车储位 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface StorageLocationMapper extends BaseDao<StorageLocationPo, Long> {

    /**
     * 批量物理删除仓库储位信息
     *
     * @param warehouseCode   仓库代码
     * @param storageAreaCode 仓库储区代码
     * @param ids             仓库储区ID数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(@Param("warehouseCode") String warehouseCode,
                             @Param("storageAreaCode") String storageAreaCode,
                             @Param("ids") Long[] ids);


    /**
     * 根据仓库、储区、储位代码查询储位
     *
     * @param warehouseCode   仓库代码
     * @param storageAreaCode 储区代码
     * @param code            储位代码
     * @return 储位
     */
    StorageLocationPo selectPoByWarehouseAreaAndCode(@Param("warehouseCode") String warehouseCode,
                                                    @Param("storageAreaCode") String storageAreaCode,
                                                    @Param("code") String code);
}
