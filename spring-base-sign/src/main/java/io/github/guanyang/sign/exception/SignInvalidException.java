package io.github.guanyang.sign.exception;

/**
 * @author gy
 */
public class SignInvalidException extends RuntimeException {

    private static final long serialVersionUID = 2415564789346816518L;

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
