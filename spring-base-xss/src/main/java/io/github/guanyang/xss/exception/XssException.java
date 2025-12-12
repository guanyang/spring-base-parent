package io.github.guanyang.xss.exception;

import lombok.Getter;

/**
 * 功能描述：xss异常定义
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
public class XssException extends RuntimeException {

    private static final long serialVersionUID = -5071736632389942822L;

    private final Object data;

    public XssException(String message) {
        this(message, null);
    }

    public XssException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public XssException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public XssException(String message, Object data, Throwable cause) {
        super(message, cause);
        this.data = data;
    }

    public XssException(Throwable cause) {
        this(null, cause);
    }
}
