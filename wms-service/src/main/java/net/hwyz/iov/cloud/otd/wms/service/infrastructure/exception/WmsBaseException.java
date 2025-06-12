package net.hwyz.iov.cloud.otd.wms.service.infrastructure.exception;


import net.hwyz.iov.cloud.framework.common.exception.BaseException;

/**
 * 仓储管理系统服务基础异常
 *
 * @author hwyz_leo
 */
public class WmsBaseException extends BaseException {

    private static final int ERROR_CODE = 302000;

    public WmsBaseException(String message) {
        super(ERROR_CODE, message);
    }

    public WmsBaseException(int errorCode) {
        super(errorCode);
    }

    public WmsBaseException(int errorCode, String message) {
        super(errorCode, message);
    }

}
