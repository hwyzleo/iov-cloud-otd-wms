package net.hwyz.iov.cloud.otd.wms.service.common.enums;

/**
 * 预入库单权威状态
 *
 * @author hwyz_leo
 */
public enum PreInboundState {

    /**
     * 待到达
     */
    PENDING_ARRIVAL,
    /**
     * 已到达
     */
    ARRIVED,
    /**
     * 已入库（完成）
     */
    COMPLETED,
    /**
     * 已取消
     */
    CANCELLED
}
