package net.hwyz.iov.cloud.otd.wms.service.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * WMS 集成测试基类
 * <p>
 * 使用真实 MySQL（Flyway 全量迁移）与真实 Kafka；数据库名按测试会话唯一生成，避免污染。
 * 连接参数可用环境变量覆盖：WMS_IT_DB_HOST/WMS_IT_DB_PORT/WMS_IT_DB_USER/WMS_IT_DB_PASSWORD/WMS_IT_KAFKA。
 *
 * @author hwyz_leo
 */
@SpringBootTest(
        classes = WmsIntegrationTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "wms.kafka.consumer.auto-startup=false",
                "wms.outbox.relay.enabled=true",
                "wms.outbox.relay.scan-interval-ms=600000",
                "wms.default-warehouse.code=WH-DEFAULT",
                "iov.kafka.topic-provisioning.enabled=false",
                "spring.main.banner-mode=off",
                "spring.autoconfigure.exclude=net.hwyz.iov.cloud.framework.audit.service.AsyncLogService,"
                        + "net.hwyz.iov.cloud.framework.audit.aspect.LogAspect,"
                        + "net.hwyz.iov.cloud.framework.security.config.WebMvcConfig,"
                        + "net.hwyz.iov.cloud.framework.security.service.TokenService,"
                        + "net.hwyz.iov.cloud.framework.security.aspect.PreAuthorizeAspect,"
                        + "net.hwyz.iov.cloud.framework.security.aspect.InnerAuthAspect,"
                        + "net.hwyz.iov.cloud.framework.security.handler.GlobalExceptionHandler,"
                        + "net.hwyz.iov.cloud.framework.security.crypto.config.CryptoAutoConfiguration,"
                        + "net.hwyz.iov.cloud.framework.security.crypto.revocation.config.PkiRevocationAutoConfiguration,"
                        + "net.hwyz.iov.cloud.framework.security.feign.ServiceCallerFeignAutoConfiguration,"
                        + "net.hwyz.iov.cloud.framework.redis.config.RedisConfig,"
                        + "net.hwyz.iov.cloud.framework.redis.config.LoadBalancerCacheConfig,"
                        + "net.hwyz.iov.cloud.framework.redis.service.RedisService"
        })
@ActiveProfiles("it")
abstract class AbstractWmsIntegrationTest {

    private static final String DATABASE_NAME = "db_wms_it_" + System.currentTimeMillis();

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        String host = env("WMS_IT_DB_HOST", "127.0.0.1");
        String port = env("WMS_IT_DB_PORT", "13306");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + DATABASE_NAME
                + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
                + "&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&preserveInstants=true"
                + "&characterEncoding=utf8";
        registry.add("spring.datasource.url", () -> url);
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        for (String table : new String[]{"tb_wms_outbox", "tb_wms_inbox", "tb_outbound_order_detail",
                "tb_outbound_order", "tb_inventory_transfer", "tb_inventory", "tb_inbound_order",
                "tb_pre_inbound_order", "tb_inventory_count_detail", "tb_inventory_count",
                "tb_storage_location", "tb_storage_area", "tb_warehouse"}) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }
}
