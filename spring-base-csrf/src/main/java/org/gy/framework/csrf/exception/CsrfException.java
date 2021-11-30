package org.gy.framework.csrf.exception;

/**
 * @author gy
 */
public class CsrfException extends RuntimeException {

    private static final long serialVersionUID = -8257418831152706667L;

    public CsrfException() {
        super();
    }

    public CsrfException(String message) {
        super(message);
    }

}
