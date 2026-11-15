package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.StorageAreaPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 整车储区 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface StorageAreaMapper extends BaseDao<StorageAreaPo, Long> {

    /**
     * 批量物理删除仓库储区信息
     *
     * @param warehouseCode 仓库代码
     * @param ids           仓库储区ID数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(@Param("warehouseCode") String warehouseCode, @Param("ids") Long[] ids);


    /**
     * 根据仓库代码与储区代码查询储区
     *
     * @param warehouseCode 仓库代码
     * @param code          储区代码
     * @return 储区
     */
    StorageAreaPo selectPoByWarehouseAndCode(@Param("warehouseCode") String warehouseCode,
                                             @Param("code") String code);
}
