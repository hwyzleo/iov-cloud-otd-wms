package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InboundOrderPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * 入库单 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface InboundOrderMapper extends BaseDao<InboundOrderPo, Long> {

    /**
     * 根据单号查询入库单
     *
     * @param orderNum 单号
     * @return 入库单
     */
    InboundOrderPo selectPoByOrderNum(String orderNum);

    /**
     * 根据预入库单号查询入库单
     *
     * @param preOrderNum 预入库单号
     * @return 入库单
     */
    InboundOrderPo selectPoByPreOrderNum(String preOrderNum);


    /**
     * 入库单分页查询（数据库过滤）
     *
     * @param orderNum       单号
     * @param preOrderNum    预入库单号
     * @param vin            车架号
     * @param warehouseLevel 仓库体系层级
     * @param warehouseCode  仓库代码
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 入库单列表
     */
    List<InboundOrderPo> search(@Param("orderNum") String orderNum,
                                @Param("preOrderNum") String preOrderNum,
                                @Param("vin") String vin,
                                @Param("warehouseLevel") String warehouseLevel,
                                @Param("warehouseCode") String warehouseCode,
                                @Param("beginTime") Date beginTime,
                                @Param("endTime") Date endTime);

    /**
     * 统计引用指定仓库／储区／储位的入库事实
     *
     * @param warehouseCode 仓库代码
     * @param areaCode      储区代码
     * @param locationCode  储位代码
     * @return 引用数量
     */
    int countReferences(@Param("warehouseCode") String warehouseCode,
                        @Param("areaCode") String areaCode,
                        @Param("locationCode") String locationCode);
}
