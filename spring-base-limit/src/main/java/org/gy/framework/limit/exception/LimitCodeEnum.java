package org.gy.framework.limit.exception;

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
public enum LimitCodeEnum {

    //成功
    EXEC_SUCCESS(0, "操作成功"),

    //失败
    EXEC_LIMIT_ERROR(500, "您操作太频繁，请稍后再试"),

    INNER_ERROR(998, "内部服务异常"),

    PARAM_SPEL_ERROR(999, "参数识别错误");

    private final int code;
    private final String msg;
}
