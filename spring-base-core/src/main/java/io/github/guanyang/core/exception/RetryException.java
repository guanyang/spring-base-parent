package io.github.guanyang.core.exception;

/**
 * 功能描述：重试异常，可以针对此异常进行重试处理
 *
 * @author gy
 * @version 1.0.0
 */
public class RetryException extends CommonException {

    private static final long serialVersionUID = 9128106426757884123L;

    public RetryException() {
        super(CommonErrorCode.SYS_OPERATE_INVALID);
    }

    public RetryException(ErrorCodeI errorCodeI) {
        super(errorCodeI);
    }

    public RetryException(ErrorCodeI errorCodeI, String msg) {
        super(errorCodeI, msg);
    }

    public RetryException(ErrorCodeI errorCodeI, Throwable e) {
        super(errorCodeI, e);
    }

    public RetryException(ErrorCodeI errorCodeI, String msg, Throwable e) {
        super(errorCodeI, msg, e);
    }

    public RetryException(int error, String msg) {
        super(error, msg);
    }

    public RetryException(int error, String msg, Throwable e) {
        super(error, msg, e);
    }
}
