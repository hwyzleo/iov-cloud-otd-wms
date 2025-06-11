package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.InboundOrderPo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po.PreInboundOrderPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 入库单 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2025-06-11
 */
@Mapper
public interface InboundOrderDao extends BaseDao<InboundOrderPo, Long> {

    /**
     * 根据单号查询入库单
     *
     * @param orderNum 单号
     * @return 入库单
     */
    InboundOrderPo selectPoByOrderNum(String orderNum);

    /**
     * 批量物理删除入库单
     *
     * @param ids 入库单ID
     * @return 删除数量
     */
    int batchPhysicalDeletePo(Long[] ids);

}
