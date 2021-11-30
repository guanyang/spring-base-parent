package org.gy.framework.xss.exception;

/**
 * 功能描述：xss异常定义
 *
 * @author gy
 * @version 1.0.0
 */
public class XssException extends RuntimeException{

    public XssException() {
        super();
    }

    public XssException(String message) {
        super(message);
    }

    public XssException(String message, Throwable cause) {
        super(message, cause);
    }

    public XssException(Throwable cause) {
        super(cause);
    }
}
