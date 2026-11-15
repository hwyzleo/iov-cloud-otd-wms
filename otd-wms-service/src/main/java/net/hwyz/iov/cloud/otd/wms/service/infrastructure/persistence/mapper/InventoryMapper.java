package net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.po.InventoryPo;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * <p>
 * 库存 Mapper
 * </p>
 *
 * @author hwyz_leo
 */
@Mapper
public interface InventoryMapper extends BaseDao<InventoryPo, Long> {

    /**
     * 根据仓库编码和车辆 VIN 查询当前库存
     *
     * @param warehouseCode 仓库代码
     * @param vin           车架号
     * @return 库存
     */
    InventoryPo selectByWarehouseCodeAndVin(@Param("warehouseCode") String warehouseCode, @Param("vin") String vin);

    /**
     * 根据 VIN 查询当前库存
     *
     * @param vin 车架号
     * @return 库存
     */
    InventoryPo selectPoByVin(String vin);

    /**
     * 预留：AVAILABLE → RESERVED（无既有预留）
     *
     * @param vin      车架号
     * @param orderNum 出库单号
     * @return 影响行数
     */
    int reserveByVin(@Param("vin") String vin, @Param("orderNum") String orderNum);

    /**
     * 释放预留：RESERVED → AVAILABLE（匹配预留单号）
     *
     * @param vin      车架号
     * @param orderNum 出库单号
     * @return 影响行数
     */
    int releaseByVin(@Param("vin") String vin, @Param("orderNum") String orderNum);

    /**
     * 出库：RESERVED → OUTBOUND（清理储位与预留）
     *
     * @param vin      车架号
     * @param orderNum 出库单号
     * @param operator 出库者
     * @param now      出库时间
     * @return 影响行数
     */
    int outboundByVin(@Param("vin") String vin, @Param("orderNum") String orderNum,
                      @Param("operator") String operator, @Param("now") Date now);

    /**
     * 移储：条件更新 AVAILABLE 库存的储区／储位
     *
     * @param id              主键
     * @param storageAreaCode 目标储区代码
     * @param storageLocationCode 目标储位代码
     * @param expectedVersion 预期行版本
     * @param operator        修改者
     * @return 影响行数
     */
    int relocateById(@Param("id") Long id,
                     @Param("storageAreaCode") String storageAreaCode,
                     @Param("storageLocationCode") String storageLocationCode,
                     @Param("expectedVersion") int expectedVersion,
                     @Param("operator") String operator);

    /**
     * 统计占用指定储位的可用／预留车辆数
     *
     * @param warehouseCode       仓库代码
     * @param storageLocationCode 储位代码
     * @return 占用数量
     */
    int countOccupiedLocation(@Param("warehouseCode") String warehouseCode,
                              @Param("storageLocationCode") String storageLocationCode);


    /**
     * 库存分页查询（数据库过滤）
     *
     * @param vin                 车架号
     * @param warehouseLevel      仓库体系层级
     * @param warehouseCode       仓库代码
     * @param state               库存状态
     * @param storageAreaCode     储区代码
     * @param storageLocationCode 储位代码
     * @param beginTime           开始时间
     * @param endTime             结束时间
     * @return 库存列表
     */
    List<InventoryPo> search(@Param("vin") String vin,
                             @Param("warehouseLevel") String warehouseLevel,
                             @Param("warehouseCode") String warehouseCode,
                             @Param("state") Integer state,
                             @Param("storageAreaCode") String storageAreaCode,
                             @Param("storageLocationCode") String storageLocationCode,
                             @Param("beginTime") Date beginTime,
                             @Param("endTime") Date endTime);

    /**
     * 统计引用指定仓库／储区／储位的库存行
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
