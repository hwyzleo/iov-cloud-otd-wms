package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging.kafka;

/**
 * WMS Kafka 逻辑 Topic 目录（Topic 注册表 SSOT）
 *
 * @author hwyz_leo
 */
public enum WmsKafkaLogicalTopic {

    /**
     * VMD 车辆生产事件（Consumer，VMD 拥有，WMS 不创建）
     */
    VEHICLE_PRODUCE(false),

    /**
     * 通用出库命令（Producer/Consumer，WMS 拥有契约并初始化）
     */
    OUTBOUND_REQUESTED(true),

    /**
     * WMS 出库事实（Producer，WMS 拥有）
     */
    OUTBOUND_CHANGED(true),

    /**
     * VMD 生产事件消费者专属死信（WMS 拥有）
     */
    VEHICLE_PRODUCE_DLQ(true),

    /**
     * 出库命令消费者专属死信（WMS 拥有）
     */
    OUTBOUND_REQUESTED_DLQ(true),

    /**
     * 通用到货命令（Producer/Consumer，WMS 拥有契约并初始化）
     */
    INBOUND_REQUESTED(true),

    /**
     * WMS 入库事实（Producer，WMS 拥有）
     */
    INBOUND_CHANGED(true),

    /**
     * 到货命令消费者专属死信（WMS 拥有）
     */
    INBOUND_REQUESTED_DLQ(true);

    private final boolean producerOwned;

    WmsKafkaLogicalTopic(boolean producerOwned) {
        this.producerOwned = producerOwned;
    }

    /**
     * 是否由 WMS 创建／初始化
     *
     * @return 是否 WMS 拥有
     */
    public boolean producerOwned() {
        return producerOwned;
    }
}
