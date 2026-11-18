package net.hwyz.iov.cloud.otd.wms.api.contract.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WMS 出库事实事件（WMS → 任意消费者，schemaVersion=2）
 * <p>
 * 消息 Key：有 WMS 订单时为 {@code outboundOrderNum}。
 * 消费者按 {@code eventId} 去重，状态投影只应用大于当前版本的 {@code aggregateVersion}。
 * <p>
 * 时间字段统一 ISO-8601（带时区偏移、固定 3 位毫秒），见 OTD-WMS-DSN-CR-002。
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleOutboundChangedEvent {

    /**
     * 事件唯一ID
     */
    private String eventId;

    /**
     * 事件类型：VehicleOutboundChangedEvent
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
     * 出库单载荷
     */
    private Payload payload;

    /**
     * 出库单事实载荷
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {

        /**
         * WMS 出库单号
         */
        private String outboundOrderNum;

        /**
         * 出库单状态
         */
        private String orderState;

        /**
         * 通用来源系统
         */
        private String sourceSystem;

        /**
         * 外部来源单号
         */
        private String sourceOrderNum;

        /**
         * 仓库代码
         */
        private String warehouseCode;

        /**
         * 导致本次流转的请求标识
         */
        private String requestId;

        /**
         * 当前 VIN 明细完整快照
         */
        private List<Item> items;
    }

    /**
     * VIN 明细快照
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        /**
         * 车架号
         */
        private String vin;

        /**
         * 明细状态
         */
        private String state;

        /**
         * 预留时间（ISO-8601）
         */
        private String reserveTime;

        /**
         * 装车时间（ISO-8601）
         */
        private String loadTime;

        /**
         * 出库时间（ISO-8601）
         */
        private String outboundTime;

        /**
         * 释放时间（ISO-8601）
         */
        private String releaseTime;
    }
}
