package org.gy.framework.core.exception;

import lombok.Getter;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
public class CommonException extends RuntimeException {

    private static final long serialVersionUID = -6529876068824192516L;
    /**
     * 错误码
     */
    private final int error;
    /**
     * 错误消息
     */
    private final String msg;

    public CommonException(ErrorCodeI errorCodeI) {
        super(errorCodeI.getMsg());
        this.error = errorCodeI.getError();
        this.msg = errorCodeI.getMsg();
    }

    public CommonException(ErrorCodeI errorCodeI, String msg) {
        super(msg);
        this.error = errorCodeI.getError();
        this.msg = msg;
    }

    public CommonException(ErrorCodeI errorCodeI, Throwable e) {
        super(errorCodeI.getMsg(), e);
        this.error = errorCodeI.getError();
        this.msg = errorCodeI.getMsg();
    }

    public CommonException(ErrorCodeI errorCodeI, String msg, Throwable e) {
        super(msg, e);
        this.error = errorCodeI.getError();
        this.msg = msg;
    }

    public CommonException(int error, String msg) {
        super(msg);
        this.error = error;
        this.msg = msg;
    }

    public CommonException(int error, String msg, Throwable e) {
        super(msg, e);
        this.error = error;
        this.msg = msg;
    }

}
