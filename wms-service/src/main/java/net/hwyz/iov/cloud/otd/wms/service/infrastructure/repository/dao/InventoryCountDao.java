package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryCountPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 盘点 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-12
 */
@Mapper
public interface InventoryCountDao extends BaseDao<InventoryCountPo, Long> {

}
