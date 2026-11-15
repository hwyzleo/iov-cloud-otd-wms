package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.WarehousePo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 整车仓库 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface WarehouseMapper extends BaseDao<WarehousePo, Long> {

    /**
     * 通过仓库代码查询仓库信息
     *
     * @param code 仓库代码
     * @return 仓库信息
     */
    WarehousePo selectPoByCode(String code);

}
