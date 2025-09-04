package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.PreInboundOrderPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 预入库单 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-06
 */
@Mapper
public interface PreInboundOrderDao extends BaseDao<PreInboundOrderPo, Long> {

    /**
     * 根据单号查询预入库单
     *
     * @param orderNum 单号
     * @return 预入库单
     */
    PreInboundOrderPo selectPoByOrderNum(String orderNum);

}
