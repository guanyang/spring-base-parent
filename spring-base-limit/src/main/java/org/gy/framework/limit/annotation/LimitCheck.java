package org.gy.framework.limit.annotation;

import org.gy.framework.limit.core.LimitKeyResolver;
import org.gy.framework.limit.core.support.ClientIpLimitKeyResolver;
import org.gy.framework.limit.core.support.ExpressionLimitKeyResolver;
import org.gy.framework.limit.core.support.GlobalLimitKeyResolver;
import org.gy.framework.limit.core.support.ServerNodeLimitKeyResolver;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 执行频率限制注解
 *
 * @author gy
 * @version 1.0.0
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LimitCheck {

    /**
     * 提示信息
     */
    String message() default "Too Many Requests";

    /**
     * 使用的 Key 解析器
     *
     * @see GlobalLimitKeyResolver 全局级别
     * @see ClientIpLimitKeyResolver 客户端IP
     * @see ServerNodeLimitKeyResolver 服务器节点
     * @see ExpressionLimitKeyResolver 自定义表达式，通过 {@link #key()} 计算
     */
    Class<? extends LimitKeyResolver> keyResolver() default ExpressionLimitKeyResolver.class;

    /**
     * 频率限制key
     */
    String key() default "";

    /**
     * 频率限制次数
     */
    int limit();

    /**
     * 限制时间，默认为 60s
     */
    int time() default 60;

    /**
     * 时间单位，默认为 SECONDS 秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * key前缀
     */
    String keyPrefix() default "limitCheck";

    /**
     * 频率限制类型，支持自定义扩展，默认支持redis
     *
     * @see org.gy.framework.limit.core.ILimitCheckService
     */
    String type() default "redis";

}
