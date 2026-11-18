package net.hwyz.iov.cloud.otd.wms.service.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * 出站事件载荷序列化器（OTD-WMS-DSN-CR-002）
 * <p>
 * 事件载荷必须是不可变且字段稳定的 JSON：保留 null 字段、不输出时间戳数值。
 * 这里用独立的序列化器，<b>不注册为 Spring Bean</b>：注册第二个 {@code ObjectMapper} 会关闭 Boot 的
 * 条件化主 mapper，导致框架 Jackson 启动探针按错误的 mapper 校验（FW-WEB-DSN-CR-003），
 * 并影响 MPT 与既有接口的全局时间输出。
 *
 * @author hwyz_leo
 */
final class WmsEventPayloadSerializer {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.ALWAYS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private WmsEventPayloadSerializer() {
    }

    /**
     * 序列化事件载荷
     *
     * @param event 事件对象
     * @return 不可变 JSON 文本
     */
    static String write(Object event) {
        try {
            return MAPPER.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("出库事件序列化失败", e);
        }
    }
}
