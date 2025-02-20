package org.gy.framework.lock.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gy.framework.lock.exception.LockCodeEnum;

import java.io.Serializable;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LockResult<T> implements Serializable {
    private static final long serialVersionUID = 4525481578052443433L;

    public static final int SUCCESS = LockCodeEnum.LOCK_SUCCESS.getCode();

    public static final String SUCCESS_MSG = LockCodeEnum.LOCK_SUCCESS.getMsg();

    public static final int ERROR = LockCodeEnum.TOO_MANY_REQUESTS.getCode();

    public static final String ERROR_MSG = LockCodeEnum.TOO_MANY_REQUESTS.getMsg();

    private int error;
    private String msg;
    private T data;

    public boolean success() {
        return error == SUCCESS;
    }

    public static <T> LockResult<T> wrapSuccess(T data) {
        return wrapResult(SUCCESS, SUCCESS_MSG, data);
    }

    public static <T> LockResult<T> wrapError() {
        return wrapResult(ERROR, ERROR_MSG, null);
    }

    public static <T> LockResult<T> wrapResult(LockCodeEnum codeEnum, T data) {
        return wrapResult(codeEnum.getCode(), codeEnum.getMsg(), data);
    }

    public static <T> LockResult<T> wrapResult(int error, String msg, T data) {
        return new LockResult<>(error, msg, data);
    }

}
