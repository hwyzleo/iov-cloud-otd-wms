package net.hwyz.iov.cloud.otd.wms.service.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 载荷摘要工具
 *
 * @author hwyz_leo
 */
public final class DigestUtil {

    private DigestUtil() {
    }

    /**
     * 计算 SHA-256 摘要（十六进制小写）
     *
     * @param payload 规范化载荷
     * @return 64 位十六进制摘要
     */
    public static String sha256Hex(String payload) {
        if (payload == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
