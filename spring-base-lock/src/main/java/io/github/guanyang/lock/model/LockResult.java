package io.github.guanyang.lock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.github.guanyang.lock.exception.LockCodeEnum;

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

    @JsonIgnore
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
