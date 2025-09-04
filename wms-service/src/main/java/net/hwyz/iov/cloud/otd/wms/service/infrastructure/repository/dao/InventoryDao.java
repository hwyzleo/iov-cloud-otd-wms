package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 库存 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-11
 */
@Mapper
public interface InventoryDao extends BaseDao<InventoryPo, Long> {

    /**
     * 根据仓库编码和车辆vin查询库存
     *
     * @param warehouseCode 仓库代码
     * @param vin           车架号
     * @return 库存
     */
    InventoryPo selectByWarehouseCodeAndVin(String warehouseCode, String vin);

}
