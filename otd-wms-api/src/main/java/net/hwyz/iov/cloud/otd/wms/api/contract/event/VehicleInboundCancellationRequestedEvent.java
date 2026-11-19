package net.hwyz.iov.cloud.otd.wms.api.contract.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用到货请求取消命令（外部请求方 → WMS，schemaVersion=2）
 * <p>
 * 消息 Key 为 {@code sourceOrderNum}；仅已受理但未入库的待入库任务可取消。
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleInboundCancellationRequestedEvent {

    /**
     * 事件唯一ID
     */
    private String eventId;

    /**
     * 事件类型：VehicleInboundCancellationRequested
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
     * 取消载荷
     */
    private Payload payload;

    /**
     * 取消载荷
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {

        /**
         * 请求幂等标识
         */
        private String requestId;

        /**
         * 通用请求方标识
         */
        private String sourceSystem;

        /**
         * 外部来源单号
         */
        private String sourceOrderNum;

        /**
         * 取消原因
         */
        private String reason;
    }
}
