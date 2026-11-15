package net.hwyz.iov.cloud.otd.wms.service.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WMS 业务类型化配置
 *
 * @author hwyz_leo
 */
@Data
@Component
@ConfigurationProperties(prefix = "wms")
public class WmsBusinessProperties {

    /**
     * 缺省仓库配置
     */
    private DefaultWarehouse defaultWarehouse = new DefaultWarehouse();

    /**
     * 缺省仓库
     */
    @Data
    public static class DefaultWarehouse {

        /**
         * 缺省仓库代码；为空表示必须显式指定仓库
         */
        private String code;
    }
}
