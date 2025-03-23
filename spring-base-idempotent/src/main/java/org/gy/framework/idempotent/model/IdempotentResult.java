package org.gy.framework.idempotent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gy.framework.idempotent.exception.IdempotentCodeEnum;
import org.gy.framework.idempotent.exception.IdempotentErrorCodeI;

import java.io.Serializable;

/**
 * 幂等结果封装
 *
 * @author gy
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdempotentResult<T> implements Serializable {
    private static final long serialVersionUID = 4525481578052443433L;

    public static final int SUCCESS = IdempotentCodeEnum.SUCCESS.getCode();

    public static final String SUCCESS_MSG = IdempotentCodeEnum.SUCCESS.getMsg();

    public static final int ERROR = IdempotentCodeEnum.TOO_MANY_REQUESTS.getCode();

    public static final String ERROR_MSG = IdempotentCodeEnum.TOO_MANY_REQUESTS.getMsg();

    private int error;
    private String msg;
    private T data;

    @JsonIgnore
    public boolean success() {
        return error == SUCCESS;
    }

    public static <T> IdempotentResult<T> wrapSuccess(T data) {
        return wrapResult(SUCCESS, SUCCESS_MSG, data);
    }

    public static <T> IdempotentResult<T> wrapError() {
        return wrapResult(ERROR, ERROR_MSG, null);
    }

    public static <T> IdempotentResult<T> wrapResult(IdempotentErrorCodeI codeEnum, T data) {
        return wrapResult(codeEnum.getCode(), codeEnum.getMsg(), data);
    }

    public static <T> IdempotentResult<T> wrapResult(int error, String msg, T data) {
        return new IdempotentResult<>(error, msg, data);
    }

}
