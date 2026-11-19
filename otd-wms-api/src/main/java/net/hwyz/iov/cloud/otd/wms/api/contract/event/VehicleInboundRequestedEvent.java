package net.hwyz.iov.cloud.otd.wms.api.contract.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用到货请求命令（外部请求方 → WMS，schemaVersion=2）
 * <p>
 * 消息 Key 为 {@code sourceOrderNum}；重试不得通过改变来源引用定位另一批车辆。
 * 本期由 TMS 在「确认交车」时生产，WMS 不依赖 TMS 模型。
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleInboundRequestedEvent {

    /**
     * 事件唯一ID
     */
    private String eventId;

    /**
     * 事件类型：VehicleInboundRequested
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
     * 命令载荷
     */
    private Payload payload;

    /**
     * 到货请求载荷
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {

        /**
         * 来源系统范围内的命令幂等标识
         */
        private String requestId;

        /**
         * 通用请求方标识
         */
        private String sourceSystem;

        /**
         * 外部来源单号（本期为运单号）
         */
        private String sourceOrderNum;

        /**
         * 目标交付库代码（必填，缺失即拒绝）
         */
        private String warehouseCode;

        /**
         * 非空、不重复的 VIN 列表，作为一个原子受理批次
         */
        private List<String> vins;

        /**
         * 预计到达时间（ISO-8601，可空）
         */
        private String estimatedArrivalTime;
    }
}
