package io.github.guanyang.core.exception;

/**
 * 功能描述：业务异常，已知的异常
 *
 * @author gy
 * @version 1.0.0
 */
public class BizException extends CommonException {

    private static final long serialVersionUID = -1857300846144229521L;

    public BizException() {
        super(CommonErrorCode.DATA_INVALID);
    }

    public BizException(ErrorCodeI errorCodeI) {
        super(errorCodeI);
    }

    public BizException(ErrorCodeI errorCodeI, String msg) {
        super(errorCodeI, msg);
    }

    public BizException(ErrorCodeI errorCodeI, Throwable e) {
        super(errorCodeI, e);
    }

    public BizException(ErrorCodeI errorCodeI, String msg, Throwable e) {
        super(errorCodeI, msg, e);
    }

    public BizException(int error, String msg) {
        super(error, msg);
    }

    public BizException(int error, String msg, Throwable e) {
        super(error, msg, e);
    }
}
