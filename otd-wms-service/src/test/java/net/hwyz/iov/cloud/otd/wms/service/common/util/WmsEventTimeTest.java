package net.hwyz.iov.cloud.otd.wms.service.common.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 出站事件时间格式化测试（OTD-WMS-DSN-CR-002）
 *
 * @author hwyz_leo
 */
class WmsEventTimeTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Instant INSTANT = Instant.parse("2026-11-18T05:05:27.412Z");

    @Test
    void shouldFormatInstantWithOffsetAndFixedMillis() {
        assertThat(WmsEventTime.format(INSTANT, SHANGHAI)).isEqualTo("2026-11-18T13:05:27.412+08:00");
    }

    @Test
    void shouldPadToThreeMillis() {
        assertThat(WmsEventTime.format(Instant.parse("2026-11-18T05:05:27.4Z"), SHANGHAI))
                .isEqualTo("2026-11-18T13:05:27.400+08:00");
        assertThat(WmsEventTime.format(Instant.parse("2026-11-18T05:05:27Z"), SHANGHAI))
                .isEqualTo("2026-11-18T13:05:27.000+08:00");
    }

    @Test
    void shouldFollowExplicitZone() {
        assertThat(WmsEventTime.format(INSTANT, ZoneOffset.UTC)).isEqualTo("2026-11-18T05:05:27.412Z");
        assertThat(WmsEventTime.format(INSTANT, ZoneId.of("America/New_York")))
                .isEqualTo("2026-11-18T00:05:27.412-05:00");
    }

    @Test
    void shouldFormatDateAndPropagateNull() {
        assertThat(WmsEventTime.format((Date) null, SHANGHAI)).isNull();
        assertThat(WmsEventTime.format(Date.from(INSTANT), SHANGHAI))
                .isEqualTo("2026-11-18T13:05:27.412+08:00");
    }

    @Test
    void shouldParseStrictIsoAndRejectOffsetlessText() {
        assertThat(WmsEventTime.parse("2026-11-18T13:05:27.412+08:00")).isEqualTo(INSTANT);
        assertThatThrownBy(() -> WmsEventTime.parse("2026-11-18T13:05:27.412"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
