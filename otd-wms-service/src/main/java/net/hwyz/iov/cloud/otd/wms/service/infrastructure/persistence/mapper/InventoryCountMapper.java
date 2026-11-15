package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryCountPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 盘点 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface InventoryCountMapper extends BaseDao<InventoryCountPo, Long> {

}
