package org.gy.framework.sign.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author gy
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface SignParam {

    String name() default "";
}
