package org.gy.framework.limit.exception;

import lombok.Getter;
import org.gy.framework.limit.annotation.LimitCheck;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
public class LimitException extends RuntimeException {

    private static final long serialVersionUID = 303295882885179551L;
    /**
     * 错误码
     */
    private final int code;
    /**
     * 错误详情
     */
    private final String msg;
    /**
     * 限流注解
     */
    private transient LimitCheck annotation;

    public LimitException(LimitCodeEnum bizCode) {
        super(bizCode.getMsg());
        this.code = bizCode.getCode();
        this.msg = bizCode.getMsg();
    }

    public LimitException(LimitCodeEnum bizCode, String msg) {
        super(msg);
        this.code = bizCode.getCode();
        this.msg = msg;
    }

    public LimitException(LimitCodeEnum bizCode, String msg, LimitCheck annotation) {
        super(msg);
        this.code = bizCode.getCode();
        this.msg = msg;
        this.annotation = annotation;
    }

    public LimitException(LimitCodeEnum bizCode, Throwable e) {
        super(bizCode.getMsg(), e);
        this.code = bizCode.getCode();
        this.msg = bizCode.getMsg();
    }

    public LimitException(LimitCodeEnum bizCode, String msg, Throwable e) {
        super(msg, e);
        this.code = bizCode.getCode();
        this.msg = msg;
    }

    public LimitException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public LimitException(int code, String msg, Throwable e) {
        super(msg, e);
        this.code = code;
        this.msg = msg;
    }

}
