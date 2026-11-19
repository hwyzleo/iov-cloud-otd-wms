package net.hwyz.iov.cloud.otd.wms.api.contract.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS 入库事实事件（WMS → 任意消费者，schemaVersion=2）
 * <p>
 * 消息 Key 为 {@code inboundTaskNum}。消费者按 {@code eventId} 去重，
 * 状态投影只应用大于当前版本的 {@code aggregateVersion}。
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleInboundChangedEvent {

    /**
     * 事件唯一ID
     */
    private String eventId;

    /**
     * 事件类型：VehicleInboundChangedEvent
     */
    private String eventType;

    /**
     * 载荷版本，固定为 2
     */
    private Integer schemaVersion;

    /**
     * 事件发生时间（ISO-8601，带偏移，固定 3 位毫秒）
     */
    private String occurredAt;

    /**
     * 生产者标识
     */
    private String producer;

    /**
     * 聚合版本
     */
    private Long aggregateVersion;

    /**
     * 待入库任务载荷
     */
    private Payload payload;

    /**
     * 待入库任务事实载荷
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {

        /**
         * WMS 待入库任务号
         */
        private String inboundTaskNum;

        /**
         * 车架号
         */
        private String vin;

        /**
         * 任务状态
         */
        private String taskState;

        /**
         * 目标生命周期段次
         */
        private Integer lifecycleSeq;

        /**
         * 通用来源系统
         */
        private String sourceSystem;

        /**
         * 外部来源单号
         */
        private String sourceOrderNum;

        /**
         * 目标交付库代码
         */
        private String warehouseCode;

        /**
         * 导致本次流转的请求标识
         */
        private String requestId;

        /**
         * 到达时间（ISO-8601）
         */
        private String arrivalTime;

        /**
         * 入库时间（ISO-8601）
         */
        private String inboundTime;

        /**
         * 取消时间（ISO-8601）
         */
        private String cancelTime;
    }
}
