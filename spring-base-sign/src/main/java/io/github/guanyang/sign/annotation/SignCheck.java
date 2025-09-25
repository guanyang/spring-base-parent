package io.github.guanyang.sign.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author gy
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface SignCheck {

    /**
     * 签名允许的时间偏移，大于0生效，单位：秒，默认30
     */
    int clockSkew() default 30;
}
