package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InboundTaskPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 交付库待入库任务 Mapper
 *
 * @author hwyz_leo
 */
@Mapper
public interface InboundTaskMapper extends BaseDao<InboundTaskPo, Long> {

    /**
     * 按任务号查询
     *
     * @param orderNum 任务号
     * @return 任务
     */
    InboundTaskPo selectPoByOrderNum(String orderNum);

    /**
     * 按来源请求标识查询
     *
     * @param sourceSystem 来源系统
     * @param requestId    请求标识
     * @return 任务
     */
    InboundTaskPo selectPoBySourceRequest(@Param("sourceSystem") String sourceSystem,
                                          @Param("requestId") String requestId);

    /**
     * 按来源单号与 VIN 查询
     *
     * @param sourceSystem   来源系统
     * @param sourceOrderNum 来源单号
     * @param vin            车架号
     * @return 任务
     */
    InboundTaskPo selectPoBySourceOrderAndVin(@Param("sourceSystem") String sourceSystem,
                                              @Param("sourceOrderNum") String sourceOrderNum,
                                              @Param("vin") String vin);

    /**
     * 条件更新任务状态与动作审计
     *
     * @param po              任务数据对象
     * @param expectedVersion 预期行版本
     * @param operator        操作人
     * @return 影响行数
     */
    int updateStateByVersion(@Param("po") InboundTaskPo po,
                             @Param("expectedVersion") int expectedVersion,
                             @Param("operator") String operator);

    /**
     * 分页查询
     *
     * @param orderNum       任务号
     * @param sourceSystem   来源系统
     * @param sourceOrderNum 来源单号
     * @param warehouseCode  仓库代码
     * @param state          状态
     * @param vin            车架号
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 任务列表
     */
    List<InboundTaskPo> search(@Param("orderNum") String orderNum,
                               @Param("sourceSystem") String sourceSystem,
                               @Param("sourceOrderNum") String sourceOrderNum,
                               @Param("warehouseCode") String warehouseCode,
                               @Param("state") String state,
                               @Param("vin") String vin,
                               @Param("beginTime") Date beginTime,
                               @Param("endTime") Date endTime);
}
