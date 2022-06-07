package org.gy.framework.limit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
     * 频率限制key
     */
    String key();

    /**
     * 频率限制次数
     */
    int limit();

    /**
     * 限制时间，单位：秒
     */
    int time() default 60;

    /**
     * 频率限制类型，支持自定义扩展，默认支持redis
     *
     * @see org.gy.framework.limit.core.ILimitCheckService
     */
    String type() default "";

}
