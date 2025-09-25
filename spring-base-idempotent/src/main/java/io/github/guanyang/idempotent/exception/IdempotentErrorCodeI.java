package io.github.guanyang.idempotent.exception;

/**
 * 功能描述：错误码定义
 *
 * @author gy
 * @version 1.0.0
 */
public interface IdempotentErrorCodeI {

    int getCode();

    String getMsg();
}
