package net.hwyz.iov.cloud.otd.wms.service.common.util;

import net.hwyz.iov.cloud.framework.common.util.ExplicitTimeUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * WMS 出站事件时间格式化工具（OTD-WMS-DSN-CR-002）
 * <p>
 * 出站事件的信封与载荷时间统一为 ISO-8601：必带时区偏移、固定 3 位毫秒。
 * 时刻来源遵循框架时间治理（FW-COMMON-DSN-CR-001）：事件时刻取 {@link ExplicitTimeUtil#nowInstant()}，
 * 时区必须显式传入，不读取 JVM 默认地区。
 * <p>
 * 纯 JDK 静态工具：无 Spring 依赖、无全局状态、无隐含时区。
 *
 * @author hwyz_leo
 */
public final class WmsEventTime {

    /**
     * 事件时间格式：ISO-8601 带偏移，固定 3 位毫秒（例 2026-11-18T13:05:27.412+08:00）
     */
    private static final DateTimeFormatter EVENT_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSXXX");

    private WmsEventTime() {
    }

    /**
     * 格式化库内物理时间（{@link Date}）为事件时间文本
     *
     * @param date 库内时间，可为 null
     * @param zone 事件时区，非 null
     * @return ISO-8601 文本；入参为 null 时返回 null
     */
    public static String format(Date date, ZoneId zone) {
        if (date == null) {
            return null;
        }
        return format(date.toInstant(), zone);
    }

    /**
     * 格式化绝对时刻为事件时间文本
     *
     * @param instant 绝对时刻，非 null
     * @param zone    事件时区，非 null
     * @return ISO-8601 文本
     */
    public static String format(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        return EVENT_FORMATTER.format(ExplicitTimeUtil.toOffsetDateTime(instant, zone));
    }

    /**
     * 解析事件时间文本（严格：必须带偏移、完整消费）
     *
     * @param text ISO-8601 文本
     * @return 绝对时刻
     */
    public static Instant parse(String text) {
        return ExplicitTimeUtil.parseInstantIso(text);
    }
}
