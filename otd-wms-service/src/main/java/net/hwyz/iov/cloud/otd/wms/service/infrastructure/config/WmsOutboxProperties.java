package net.hwyz.iov.cloud.otd.wms.service.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WMS Outbox 中继类型化配置
 *
 * @author hwyz_leo
 */
@Data
@Component
@ConfigurationProperties(prefix = "wms.outbox.relay")
public class WmsOutboxProperties {

    /**
     * 是否启用中继
     */
    private boolean enabled = true;

    /**
     * 扫描间隔（毫秒）
     */
    private long scanIntervalMs = 10000;

    /**
     * 单批最大记录数
     */
    private int batchSize = 100;

    /**
     * 最大重试次数
     */
    private int maxRetry = 5;

    /**
     * 重试间隔（秒）
     */
    private int retryIntervalSeconds = 30;
}
