package net.hwyz.iov.cloud.otd.wms.service.common.enums;

/**
 * 交付库待入库任务状态
 *
 * @author hwyz_leo
 */
public enum InboundTaskState {

    /**
     * 到货请求已受理，等待车辆抵达交付库
     */
    PENDING,
    /**
     * 交付库已确认到货，等待分配储区／储位
     */
    ARRIVED,
    /**
     * 入库完成，已生成第二段入库事实
     */
    COMPLETED,
    /**
     * 装车前取消，库存回退为 OUTBOUND
     */
    CANCELLED
}
