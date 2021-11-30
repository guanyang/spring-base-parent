package org.gy.framework.sign.exception;

/**
 * @author gy
 */
public class SignInvalidException extends RuntimeException {

    public SignInvalidException() {
        super();
    }

    public SignInvalidException(String message) {
        super(message);
    }

    public SignInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

}
