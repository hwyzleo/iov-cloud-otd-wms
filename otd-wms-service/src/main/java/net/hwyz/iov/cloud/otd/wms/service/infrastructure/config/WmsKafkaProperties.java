package net.hwyz.iov.cloud.otd.wms.service.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * WMS Kafka 类型化配置（Topic 注册表、消费组、重试与创建参数）
 *
 * @author hwyz_leo
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "wms.kafka")
public class WmsKafkaProperties {

    /**
     * Topic 注册表
     */
    private Topics topics = new Topics();

    /**
     * 消费者配置
     */
    private Consumer consumer = new Consumer();

    /**
     * Topic 创建参数
     */
    private Provisioning provisioning = new Provisioning();

    /**
     * Topic 注册表（单一事实来源，禁止字符串拼接推导）
     */
    @Data
    public static class Topics {

        /**
         * VMD 车辆生产事件 Topic（VMD 拥有，WMS 只消费不创建）
         */
        @NotBlank
        private String vehicleProduce = "vmd.vehicle-produce";

        /**
         * 通用出库命令 Topic（WMS 拥有契约并初始化）
         */
        @NotBlank
        private String outboundRequested = "wms.vehicle-outbound.requested";

        /**
         * WMS 出库事实 Topic（WMS 生产并拥有）
         */
        @NotBlank
        private String outboundChanged = "wms.vehicle-outbound.changed";

        /**
         * VMD 生产事件消费者专属死信 Topic
         */
        @NotBlank
        private String vehicleProduceDlq = "vmd.vehicle-produce.wms.dlq";

        /**
         * 出库命令消费者专属死信 Topic
         */
        @NotBlank
        private String outboundRequestedDlq = "wms.vehicle-outbound.requested.wms.dlq";
    }

    /**
     * 消费者配置
     */
    @Data
    public static class Consumer {

        /**
         * VMD 生产事件消费组
         */
        @NotBlank
        private String vehicleProduceGroup = "otd-wms-vehicle-produce";

        /**
         * 出库命令消费组
         */
        @NotBlank
        private String outboundCommandGroup = "otd-wms-vehicle-outbound-command";

        /**
         * 是否自动启动消费者
         */
        private boolean autoStartup = true;

        /**
         * 有界重试配置
         */
        private Retry retry = new Retry();
    }

    /**
     * 有界重试配置
     */
    @Data
    public static class Retry {

        /**
         * 最大投递次数（含首次）
         */
        @Min(1)
        private long maxAttempts = 3;

        /**
         * 重试退避间隔（毫秒）
         */
        @Min(0)
        private long backoffMs = 1000;
    }

    /**
     * Topic 创建参数
     */
    @Data
    public static class Provisioning {

        /**
         * 分区数
         */
        @Min(1)
        private int partitions = 3;

        /**
         * 副本数
         */
        @Min(1)
        private short replicationFactor = 1;
    }
}
