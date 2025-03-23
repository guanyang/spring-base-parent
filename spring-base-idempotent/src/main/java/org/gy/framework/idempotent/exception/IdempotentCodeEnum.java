package org.gy.framework.idempotent.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum IdempotentCodeEnum implements IdempotentErrorCodeI {

    //成功
    SUCCESS(0, "操作成功"),

    TOO_MANY_REQUESTS(429, "Too Many Requests"),

    //失败
    ERROR(500, "操作失败"),

    INNER_ERROR(998, "内部服务异常"),

    PARAM_SPEL_ERROR(999, "参数识别错误");

    private final int code;
    private final String msg;
}
