package net.hwyz.iov.cloud.otd.wms.service.common.exception;

import net.hwyz.iov.cloud.framework.common.exception.BusinessException;
import net.hwyz.iov.cloud.framework.common.exception.ErrorCode;

/**
 * WMS 业务异常
 * <p>
 * 错误码统一来自 {@link WmsErrorCode}，不使用散装数值。
 *
 * @author hwyz_leo
 */
public class WmsBusinessException extends BusinessException {

    public WmsBusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public WmsBusinessException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public static WmsBusinessException of(WmsErrorCode errorCode) {
        return new WmsBusinessException(errorCode);
    }

    public static WmsBusinessException of(WmsErrorCode errorCode, String detail) {
        return new WmsBusinessException(errorCode, detail);
    }
}
