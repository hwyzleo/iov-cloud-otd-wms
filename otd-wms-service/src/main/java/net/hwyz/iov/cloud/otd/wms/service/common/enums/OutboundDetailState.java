package net.hwyz.iov.cloud.otd.wms.service.common.enums;

/**
 * 出库单 VIN 明细状态
 *
 * @author hwyz_leo
 */
public enum OutboundDetailState {

    /**
     * 已预留，库存保持 RESERVED 并占用储位
     */
    RESERVED,
    /**
     * 已装车，库存继续 RESERVED 并占用储位
     */
    LOADED,
    /**
     * 已出库，库存改为 OUTBOUND 并释放储位
     */
    OUTBOUND,
    /**
     * 装车前取消，库存释放为 AVAILABLE
     */
    RELEASED
}
