package net.hwyz.iov.cloud.otd.wms.service.common.enums;

/**
 * 出库单状态
 *
 * @author hwyz_leo
 */
public enum OutboundOrderState {

    /**
     * 全部 VIN 已预留，尚未装车
     */
    RESERVED,
    /**
     * 部分 VIN 已装车
     */
    PARTIALLY_LOADED,
    /**
     * 全部 VIN 已装车
     */
    LOADED,
    /**
     * 部分 VIN 已出库
     */
    PARTIALLY_OUTBOUND,
    /**
     * 全部 VIN 已出库
     */
    COMPLETED,
    /**
     * 装车前取消，全部预留已释放
     */
    CANCELLED
}
