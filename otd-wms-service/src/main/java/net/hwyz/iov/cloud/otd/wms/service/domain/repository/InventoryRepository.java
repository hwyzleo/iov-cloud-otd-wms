package net.hwyz.iov.cloud.otd.wms.service.domain.repository;

import net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate.VehicleInventory;
import net.hwyz.iov.cloud.otd.wms.service.domain.model.valueobject.InventoryCriteria;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 当前库存仓储（领域定义的持久化端口）
 *
 * @author hwyz_leo
 */
public interface InventoryRepository {

    /**
     * 按主键查询
     *
     * @param id 主键
     * @return 当前库存
     */
    Optional<VehicleInventory> findById(Long id);

    /**
     * 按 VIN 查询当前库存
     *
     * @param vin 车架号
     * @return 当前库存
     */
    Optional<VehicleInventory> findByVin(String vin);

    /**
     * 写入 AVAILABLE 库存（VIN 唯一与储位占用由数据库约束保证）
     *
     * @param inventory 当前库存
     */
    void insert(VehicleInventory inventory);

    /**
     * 预留：条件更新 AVAILABLE → RESERVED，无既有预留
     *
     * @param vin      车架号
     * @param orderNum 出库单号
     * @return 影响行数（0 表示不可预留）
     */
    int reserve(String vin, String orderNum);

    /**
     * 释放预留：条件更新 RESERVED → AVAILABLE，匹配预留单号
     *
     * @param vin      车架号
     * @param orderNum 出库单号
     * @return 影响行数
     */
    int release(String vin, String orderNum);

    /**
     * 出库：条件更新 RESERVED → OUTBOUND，清理储位与预留
     *
     * @param vin      车架号
     * @param orderNum 出库单号
     * @param operator 可信操作人
     * @param now      WMS 生成的出库时间
     * @return 影响行数
     */
    int markOutbound(String vin, String orderNum, String operator, Date now);

    /**
     * 同仓移储：条件更新 AVAILABLE 库存储位
     *
     * @param id              库存主键
     * @param newAreaCode     目标储区代码
     * @param newLocationCode 目标储位代码
     * @param expectedVersion 预期行版本
     * @param operator        可信操作人
     * @return 影响行数（0 表示版本冲突或状态不符）
     */
    int relocate(Long id, String newAreaCode, String newLocationCode, int expectedVersion, String operator);

    /**
     * 储位是否被 AVAILABLE／RESERVED 车辆占用
     *
     * @param warehouseCode     仓库代码
     * @param storageLocationCode 储位代码
     * @return 是否占用
     */
    boolean isLocationOccupied(String warehouseCode, String storageLocationCode);

    /**
     * 查询
     *
     * @param criteria 查询条件
     * @return 库存列表
     */
    List<VehicleInventory> search(InventoryCriteria criteria);
}
