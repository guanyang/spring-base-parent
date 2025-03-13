package org.gy.framework.core.dto;

import lombok.Getter;
import lombok.Setter;
import org.gy.framework.core.exception.CommonErrorCode;
import org.gy.framework.core.exception.CommonException;
import org.gy.framework.core.exception.ErrorCodeI;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
@Setter
public class Response<T> extends BaseResponse {

    private static final long serialVersionUID = 406754594494048189L;

    private T data;

    public static <T> Response<T> asSuccess(String msg, T data) {
        Response<T> response = new Response<>();
        response.wrapResponse(SUCCESS_CODE, msg);
        response.setData(data);
        return response;
    }

    public static <T> Response<T> asSuccess(T data) {
        return asSuccess(SUCCESS_MSG, data);
    }

    public static <T> Response<T> asSuccess() {
        return asSuccess(SUCCESS_MSG, null);
    }

    public static <T> Response<T> asError(int error, String msg, T data) {
        Response<T> response = new Response<>();
        response.wrapResponse(error, msg);
        response.setData(data);
        return response;
    }

    public static <T> Response<T> asError(int error, String msg) {
        return asError(error, msg, null);
    }

    public static <T> Response<T> asError(String msg) {
        return asError(ERROR_CODE, msg, null);
    }

    public static <T> Response<T> asError(ErrorCodeI bizCode) {
        return asError(bizCode.getError(), bizCode.getMsg(), null);
    }

    public static <T> Response<T> asError(CommonException e) {
        return asError(e.getError(), e.getMsg(), null);
    }

    public static <T> Response<T> asError() {
        return asError(ERROR_CODE, ERROR_MSG, null);
    }

}
