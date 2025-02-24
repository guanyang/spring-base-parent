package org.gy.framework.idempotent.annotation;

import org.gy.framework.idempotent.core.IdempotentKeyResolver;
import org.gy.framework.idempotent.core.support.DefaultIdempotentKeyResolver;
import org.gy.framework.idempotent.core.support.ExpressionIdempotentKeyResolver;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等注解
 *
 * @author gy
 * @version 1.0.0
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等的超时时间，默认为 1 秒
     */
    int timeout() default 1;

    /**
     * 时间单位，默认为 SECONDS 秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 提示信息，正在执行中的提示
     */
    String message() default "Too Many Requests";

    /**
     * 使用的 Key 解析器
     *
     * @see DefaultIdempotentKeyResolver 全局级别
     * @see ExpressionIdempotentKeyResolver 自定义表达式，通过 {@link #keyArg()} 计算
     */
    Class<? extends IdempotentKeyResolver> keyResolver() default DefaultIdempotentKeyResolver.class;

    /**
     * 使用的 Key 参数
     */
    String keyArg() default "";

    /**
     * key前缀
     */
    String keyPrefix() default "idempotent";

    /**
     * 删除 Key，当发生异常时候
     */
    boolean deleteKeyWhenException() default true;
}
