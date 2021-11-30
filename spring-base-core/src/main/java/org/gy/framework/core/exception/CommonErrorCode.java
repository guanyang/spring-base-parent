package org.gy.framework.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 功能描述：错误码统一维护
 * <ul>
 * <strong>请严格遵守以下规则，否则后果自负!</strong>
 * <li>错误码格式要求：{3位功能位}{3位错误位}，一共六位</li>
 * </ul>
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCodeI {

    /**
     * 错误码定义
     * <ul>
     * <strong>请严格遵守以下规则，否则后果自负!</strong>
     * <li>错误码格式要求：{3位功能位}{3位错误位}，一共六位</li>
     * </ul>
     */
    USER_STATUS_ERROR(100001, "请登录"),
    USER_STATUS_BANNED(100002, "账号被封禁"),

    PARAM_ERROR(101001, "参数有误"),
    PARAM_XSS_ERROR(101002, "存在XSS风险"),
    PARAM_CSRF_ERROR(101003, "请求校验不通过"),
    PARAM_BAD_REQUEST(101004, "输入的查询条件错误"),

    DATA_NOT_EXIST(102001, "对象不存在"),
    DATA_INVALID(102002, "内部数据不合法"),

    /***************定义业务错误码 start******************/



    /***************定义业务错误码 end******************/

    SYS_OPERATE_INVALID(999001, "网络繁忙，请稍后重试"),
    SYS_SERVICE_RETURN_ERROR(999002, "依赖服务出现错误"),
    SYS_SERVICE_ERROR(999003, "服务异常，请稍后重试");

    private final int error;
    private final String msg;


}
