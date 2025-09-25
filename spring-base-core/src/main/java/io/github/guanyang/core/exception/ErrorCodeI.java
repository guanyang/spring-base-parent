package io.github.guanyang.core.exception;

import io.github.guanyang.core.support.IStdEnum;

/**
 * 功能描述：错误码定义
 *
 * @author gy
 * @version 1.0.0
 */
public interface ErrorCodeI extends IStdEnum<Integer> {

    int getError();

    String getMsg();

    @Override
    default Integer getCode() {
        return getError();
    }

    @Override
    default String getDesc() {
        return getMsg();
    }
}
