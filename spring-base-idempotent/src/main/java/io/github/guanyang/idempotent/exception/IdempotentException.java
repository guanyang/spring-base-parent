package io.github.guanyang.idempotent.exception;

import lombok.Getter;
import io.github.guanyang.idempotent.annotation.Idempotent;

@Getter
public class IdempotentException extends RuntimeException {
    private static final long serialVersionUID = 8109454344203101561L;
    /**
     * 错误码
     */
    private final int code;
    /**
     * 错误详情
     */
    private final String msg;
    /**
     * Idempotent注解
     */
    private transient Idempotent annotation;

    public IdempotentException(IdempotentErrorCodeI bizCode) {
        super(bizCode.getMsg());
        this.code = bizCode.getCode();
        this.msg = bizCode.getMsg();
    }

    public IdempotentException(IdempotentErrorCodeI bizCode, String msg) {
        super(msg);
        this.code = bizCode.getCode();
        this.msg = msg;
    }

    public IdempotentException(IdempotentErrorCodeI bizCode, String msg, Idempotent annotation) {
        super(msg);
        this.code = bizCode.getCode();
        this.msg = msg;
        this.annotation = annotation;
    }

    public IdempotentException(IdempotentErrorCodeI bizCode, Throwable e) {
        super(bizCode.getMsg(), e);
        this.code = bizCode.getCode();
        this.msg = bizCode.getMsg();
    }

    public IdempotentException(IdempotentErrorCodeI bizCode, String msg, Throwable e) {
        super(msg, e);
        this.code = bizCode.getCode();
        this.msg = msg;
    }


    public IdempotentException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public IdempotentException(Integer code, String msg, Throwable e) {
        super(msg, e);
        this.code = code;
        this.msg = msg;
    }
}
