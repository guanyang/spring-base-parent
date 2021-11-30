package org.gy.framework.sign.annotation;

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
}
