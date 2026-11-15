package net.hwyz.iov.cloud.otd.wms.service.common.util;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;

/**
 * 单号生成器
 * <p>
 * 业务单号 = 前缀 + 毫秒时间戳 + 3 位随机数，保持基线可读格式。
 *
 * @author hwyz_leo
 */
public final class OrderNumGenerator {

    /**
     * 预入库单前缀
     */
    public static final String PRE_INBOUND_PREFIX = "PIO";
    /**
     * 入库单前缀
     */
    public static final String INBOUND_PREFIX = "IO";
    /**
     * 出库单前缀
     */
    public static final String OUTBOUND_PREFIX = "OUT";

    private OrderNumGenerator() {
    }

    /**
     * 生成业务单号
     *
     * @param prefix 前缀
     * @return 单号
     */
    public static String generate(String prefix) {
        return prefix + LocalDateTimeUtil.format(LocalDateTimeUtil.now(), "yyyyMMddHHmmssSSS")
                + RandomUtil.randomNumbers(3);
    }
}
