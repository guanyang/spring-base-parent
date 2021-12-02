package org.gy.framework.log.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author gy
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
public @interface LogTrace {

    /**
     * 业务参数名称，传值则获取指定参数报文，不传则获取方法所有参数报文
     */
    String[] fieldName() default {};

    /**
     * 操作描述
     */
    String desc() default "default";

}
