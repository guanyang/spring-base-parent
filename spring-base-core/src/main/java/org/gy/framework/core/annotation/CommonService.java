package org.gy.framework.core.annotation;

import java.lang.annotation.*;

/**
 * 通用Service注解
 *
 * @author gy
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommonService {

    String value() default "";
}
