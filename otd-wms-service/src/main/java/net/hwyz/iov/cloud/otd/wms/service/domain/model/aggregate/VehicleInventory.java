package net.hwyz.iov.cloud.otd.wms.service.domain.model.aggregate;

import lombok.Builder;
import lombok.Getter;
import net.hwyz.iov.cloud.otd.wms.service.common.enums.InventoryState;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsBusinessException;
import net.hwyz.iov.cloud.otd.wms.service.common.exception.WmsErrorCode;

import java.util.Date;

/**
 * 车辆当前库存聚合根
 * <p>
 * 一个 VIN 一条当前记录；AVAILABLE／RESERVED 占用唯一储位，OUTBOUND 释放储位。
 *
 * @author hwyz_leo
 */
@Getter
@Builder
public class VehicleInventory {

    /**
     * 主键
     */
    private Long id;
    /**
     * 车架号
     */
    private String vin;
    /**
     * 生产配置代码
     */
    private String buildConfigCode;
    /**
     * 仓库代码
     */
    private String warehouseCode;
    /**
     * 仓库名称（查询投影）
     */
    private String warehouseName;
    /**
     * 仓库体系层级（查询投影）
     */
    private String warehouseLevel;
    /**
     * 储区代码
     */
    private String storageAreaCode;
    /**
     * 储位代码
     */
    private String storageLocationCode;
    /**
     * 库存状态
     */
    private InventoryState state;
    /**
     * 当前预留出库单号
     */
    private String reservedOrderNum;
    /**
     * 入库时间
     */
    private Date inboundTime;
    /**
     * 入库者
     */
    private String inboundBy;
    /**
     * 出库时间
     */
    private Date outboundTime;
    /**
     * 出库者
     */
    private String outboundBy;
    /**
     * 记录版本
     */
    private Integer rowVersion;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 预留：AVAILABLE → RESERVED，保留储位占用
     *
     * @param orderNum 出库单号
     */
    public void reserve(String orderNum) {
        if (state != InventoryState.AVAILABLE || reservedOrderNum != null) {
            throw WmsBusinessException.of(WmsErrorCode.VIN_NOT_AVAILABLE,
                    "车辆[" + vin + "]当前不可预留，状态[" + state + "]");
        }
        this.state = InventoryState.RESERVED;
        this.reservedOrderNum = orderNum;
    }

    /**
     * 取消预留：RESERVED → AVAILABLE，释放预留单号
     *
     * @param orderNum 出库单号
     */
    public void release(String orderNum) {
        requireReversedBy(orderNum);
        this.state = InventoryState.AVAILABLE;
        this.reservedOrderNum = null;
    }

    /**
     * 出库：RESERVED → OUTBOUND，清除储区／储位／预留单号并保留出库审计
     *
     * @param orderNum 出库单号
     * @param operator 可信操作人
     * @param now      WMS 生成的出库时间
     */
    public void markOutbound(String orderNum, String operator, Date now) {
        requireReversedBy(orderNum);
        this.state = InventoryState.OUTBOUND;
        this.storageAreaCode = null;
        this.storageLocationCode = null;
        this.reservedOrderNum = null;
        this.outboundTime = now;
        this.outboundBy = operator;
    }

    /**
     * 同仓移储：仅 AVAILABLE 允许，目标储位已由仓储主数据校验
     *
     * @param newStorageAreaCode     目标储区代码
     * @param newStorageLocationCode 目标储位代码
     */
    public void relocate(String newStorageAreaCode, String newStorageLocationCode) {
        if (state != InventoryState.AVAILABLE) {
            throw WmsBusinessException.of(WmsErrorCode.VIN_NOT_AVAILABLE,
                    "车辆[" + vin + "]状态为[" + state + "]，不允许移储");
        }
        this.storageAreaCode = newStorageAreaCode;
        this.storageLocationCode = newStorageLocationCode;
    }

    private void requireReversedBy(String orderNum) {
        if (state != InventoryState.RESERVED || !orderNum.equals(reservedOrderNum)) {
            throw WmsBusinessException.of(WmsErrorCode.VIN_NOT_AVAILABLE,
                    "车辆[" + vin + "]预留信息不匹配，状态[" + state + "]，预留单号[" + reservedOrderNum + "]");
        }
    }
}
