package net.hwyz.iov.cloud.otd.wms.api.contract.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WMS 出库命令拒绝事件（WMS → 任意消费者，schemaVersion=2）
 * <p>
 * 合法业务拒绝是已完成处理结果；该事件不改变库存或订单状态，也不推进聚合版本。
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleOutboundCommandRejectedEvent {

    /**
     * 事件唯一ID
     */
    private String eventId;

    /**
     * 事件类型：VehicleOutboundCommandRejectedEvent
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
     * 聚合版本（无订单时为 0）
     */
    private Long aggregateVersion;

    /**
     * 拒绝载荷
     */
    private Payload payload;

    /**
     * 拒绝载荷
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {

        /**
         * 导致拒绝的请求标识
         */
        private String requestId;

        /**
         * 通用来源系统
         */
        private String sourceSystem;

        /**
         * 外部来源单号
         */
        private String sourceOrderNum;

        /**
         * WMS 出库单号（未创建订单时为空）
         */
        private String outboundOrderNum;

        /**
         * 拒绝原因错误码
         */
        private String reasonCode;

        /**
         * 逐 VIN 原因
         */
        private List<ItemReason> itemReasons;
    }

    /**
     * 逐 VIN 拒绝原因
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemReason {

        /**
         * 车架号
         */
        private String vin;

        /**
         * 错误码
         */
        private String reasonCode;
    }
}
