package net.hwyz.iov.cloud.otd.wms.service.integration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 集成测试应用骨架
 * <p>
 * 只装配 WMS 领域／应用／基础设施／接入层，排除真实启动类（避免 Nacos 发现与配置）。
 *
 * @author hwyz_leo
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
        basePackages = {
                "net.hwyz.iov.cloud.otd.wms.service.application",
                "net.hwyz.iov.cloud.otd.wms.service.infrastructure",
                "net.hwyz.iov.cloud.otd.wms.service.adapter"
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = ".*adapter\\.mq\\.consumer\\..*"))
@MapperScan("net.hwyz.iov.cloud.otd.wms.service.infrastructure.persistence.mapper")
public class WmsIntegrationTestApplication {
}
