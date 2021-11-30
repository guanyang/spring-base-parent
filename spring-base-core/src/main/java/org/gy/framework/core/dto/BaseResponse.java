package org.gy.framework.core.dto;

import lombok.Data;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Data
public abstract class BaseResponse extends DTO {

    private static final long serialVersionUID = 6761677008678901732L;

    public static final int SUCCESS_CODE = 0;
    public static final int ERROR_CODE = -1;
    public static final String SUCCESS_MSG = "success";
    public static final String ERROR_MSG = "fail";
    public static final int BAD_REQUEST_CODE = 400;
    public static final int SERVER_ERROR_CODE = 500;
    public static final int USER_FORBIDDEN_CODE = 403;
    public static final int REDIRECT_CODE = 302;

    private int error = SUCCESS_CODE;
    private String msg = SUCCESS_MSG;

    public boolean success() {
        return error == SUCCESS_CODE;
    }

    public void wrapResponse(int error, String msg) {
        this.setError(error);
        this.setMsg(msg);
    }
}
