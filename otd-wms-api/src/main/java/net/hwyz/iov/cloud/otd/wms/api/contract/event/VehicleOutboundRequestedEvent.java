package net.hwyz.iov.cloud.otd.wms.api.contract.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用出库命令（外部请求方 → WMS，schemaVersion=1）
 * <p>
 * 消息 Key 为 {@code sourceOrderNum}；重试不得通过改变来源引用定位另一张订单。
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleOutboundRequestedEvent {

    /**
     * 事件唯一ID
     */
    private String eventId;

    /**
     * 事件类型：VehicleOutboundRequested
     */
    private String eventType;

    /**
     * 载荷版本，固定为 1
     */
    private Integer schemaVersion;

    /**
     * 事件发生时间（epoch 毫秒）
     */
    private Long occurredAtEpochMillis;

    /**
     * 生产者标识
     */
    private String producer;

    /**
     * 命令载荷
     */
    private Payload payload;

    /**
     * 出库命令载荷
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
         * 外部来源单号
         */
        private String sourceOrderNum;

        /**
         * 显式目标仓库代码（缺省时使用默认仓库）
         */
        private String warehouseCode;

        /**
         * 非空、不重复的 VIN 列表，作为一个原子预留批次
         */
        private List<String> vins;
    }
}
