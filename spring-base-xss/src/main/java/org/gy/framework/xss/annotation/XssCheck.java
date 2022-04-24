package org.gy.framework.xss.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Xss检查注解，不添加注解时所有String类型全部默认开启，该注解可以让特殊场景不开启
 *
 * @author gy
 * @version 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XssCheck {

    /**
     * 是否开启xss检查，默认开启
     */
    boolean check() default true;

    /**
     * 是否开启去空格处理，默认开启
     */
    boolean trim() default true;

}
