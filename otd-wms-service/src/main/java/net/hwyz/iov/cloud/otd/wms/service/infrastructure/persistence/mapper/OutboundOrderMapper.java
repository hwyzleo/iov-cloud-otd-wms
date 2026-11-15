package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.OutboundOrderPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 出库单 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrderPo> {

    /**
     * 按单号查询
     *
     * @param orderNum 出库单号
     * @return 出库单
     */
    OutboundOrderPo selectPoByOrderNum(String orderNum);

    /**
     * 按来源请求标识查询
     *
     * @param sourceSystem 来源系统
     * @param requestId    请求标识
     * @return 出库单
     */
    OutboundOrderPo selectPoBySourceRequest(@Param("sourceSystem") String sourceSystem,
                                            @Param("requestId") String requestId);

    /**
     * 按来源单号查询
     *
     * @param sourceSystem   来源系统
     * @param sourceOrderNum 来源单号
     * @return 出库单
     */
    OutboundOrderPo selectPoBySourceOrder(@Param("sourceSystem") String sourceSystem,
                                          @Param("sourceOrderNum") String sourceOrderNum);

    /**
     * 条件更新订单状态与聚合版本
     *
     * @param id               主键
     * @param state            订单状态
     * @param aggregateVersion 聚合版本
     * @param operator         修改者
     * @param expectedVersion  预期行版本
     * @return 影响行数
     */
    int updateStateByVersion(@Param("id") Long id,
                             @Param("state") String state,
                             @Param("aggregateVersion") Long aggregateVersion,
                             @Param("operator") String operator,
                             @Param("expectedVersion") int expectedVersion);

    /**
     * 出库单分页查询（数据库过滤）
     *
     * @param orderNum       出库单号
     * @param sourceSystem   来源系统
     * @param sourceOrderNum 来源单号
     * @param warehouseCode  仓库代码
     * @param state          出库单状态
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 出库单列表
     */
    List<OutboundOrderPo> search(@Param("orderNum") String orderNum,
                                 @Param("sourceSystem") String sourceSystem,
                                 @Param("sourceOrderNum") String sourceOrderNum,
                                 @Param("warehouseCode") String warehouseCode,
                                 @Param("state") String state,
                                 @Param("beginTime") Date beginTime,
                                 @Param("endTime") Date endTime);

}
