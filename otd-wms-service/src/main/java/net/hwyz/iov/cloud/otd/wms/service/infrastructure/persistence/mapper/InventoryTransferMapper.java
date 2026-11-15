package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryTransferPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * 移库 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface InventoryTransferMapper extends BaseDao<InventoryTransferPo, Long> {

    /**
     * 根据请求标识查询移储记录
     *
     * @param requestId 请求标识
     * @return 移储记录
     */
    InventoryTransferPo selectPoByRequestId(String requestId);


    /**
     * 移储分页查询（数据库过滤）
     *
     * @param vin            车架号
     * @param warehouseLevel 仓库体系层级
     * @param warehouseCode  仓库代码
     * @param beginTime      开始时间
     * @param endTime        结束时间
     * @return 移储列表
     */
    List<InventoryTransferPo> search(@Param("vin") String vin,
                                     @Param("warehouseLevel") String warehouseLevel,
                                     @Param("warehouseCode") String warehouseCode,
                                     @Param("beginTime") Date beginTime,
                                     @Param("endTime") Date endTime);

    /**
     * 统计引用指定仓库／储区／储位的移储事实
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
