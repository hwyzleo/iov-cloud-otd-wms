package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InventoryTransferPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 移库 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-12
 */
@Mapper
public interface InventoryTransferDao extends BaseDao<InventoryTransferPo, Long> {

}
