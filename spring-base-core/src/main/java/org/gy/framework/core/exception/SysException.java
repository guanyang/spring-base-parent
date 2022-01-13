package org.gy.framework.core.exception;

/**
 * 功能描述：系统异常
 *
 * @author gy
 * @version 1.0.0
 */
public class SysException extends CommonException {

    private static final long serialVersionUID = -8095603068753036596L;

    public SysException() {
        super(CommonErrorCode.SYS_SERVICE_ERROR);
    }

    public SysException(ErrorCodeI errorCodeI) {
        super(errorCodeI);
    }

    public SysException(ErrorCodeI errorCodeI, String msg) {
        super(errorCodeI, msg);
    }

    public SysException(ErrorCodeI errorCodeI, Throwable e) {
        super(errorCodeI, e);
    }

    public SysException(ErrorCodeI errorCodeI, String msg, Throwable e) {
        super(errorCodeI, msg, e);
    }

    public SysException(int error, String msg) {
        super(error, msg);
    }

    public SysException(int error, String msg, Throwable e) {
        super(error, msg, e);
    }
}
