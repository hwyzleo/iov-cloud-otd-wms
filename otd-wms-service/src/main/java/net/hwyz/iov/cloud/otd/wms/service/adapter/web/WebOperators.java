package net.hwyz.iov.cloud.otd.wms.service.adapter.web;

import net.hwyz.iov.cloud.framework.security.util.SecurityUtils;

/**
 * 可信操作人解析
 * <p>
 * 操作人只来自安全上下文，不接受请求传参。
 *
 * @author hwyz_leo
 */
public final class WebOperators {

    /**
     * 无安全上下文时的系统操作人标识
     */
    private static final String SYSTEM_OPERATOR = "0";

    private WebOperators() {
    }

    /**
     * 当前可信操作人
     *
     * @return 操作人标识
     */
    public static String currentOperator() {
        try {
            Long userId = SecurityUtils.getUserId();
            return userId == null ? SYSTEM_OPERATOR : userId.toString();
        } catch (RuntimeException e) {
            return SYSTEM_OPERATOR;
        }
    }
}
