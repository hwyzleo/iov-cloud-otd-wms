package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.PreInboundOrderPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 预入库单 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface PreInboundOrderMapper extends BaseDao<PreInboundOrderPo, Long> {

    /**
     * 根据单号查询预入库单
     *
     * @param orderNum 单号
     * @return 预入库单
     */
    PreInboundOrderPo selectPoByOrderNum(String orderNum);

    /**
     * 根据 VIN 查询预入库单
     *
     * @param vin 车架号
     * @return 预入库单
     */
    PreInboundOrderPo selectPoByVin(String vin);

    /**
     * 按行版本与前置状态条件更新（CAS）
     *
     * @param po              预入库单
     * @param expectedVersion 预期行版本
     * @param transitFrom     允许的前置状态
     * @param operator        修改者
     * @return 影响行数
     */
    int updateStateByVersion(@Param("po") PreInboundOrderPo po,
                             @Param("expectedVersion") int expectedVersion,
                             @Param("transitFrom") String transitFrom,
                             @Param("operator") String operator);


    /**
     * 预入库单分页查询（数据库过滤）
     *
     * @param orderNum       单号
     * @param vin            车架号
     * @param warehouseLevel 仓库体系层级
     * @param warehouseCode  仓库代码
     * @param state          权威状态
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 预入库单列表
     */
    List<PreInboundOrderPo> search(@Param("orderNum") String orderNum,
                                   @Param("vin") String vin,
                                   @Param("warehouseLevel") String warehouseLevel,
                                   @Param("warehouseCode") String warehouseCode,
                                   @Param("state") String state,
                                   @Param("beginTime") Date beginTime,
                                   @Param("endTime") Date endTime);
}
