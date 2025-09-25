package io.github.guanyang.xss.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.github.guanyang.xss.XssCommon;
import org.springframework.context.annotation.Import;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(XssCommon.class)
public @interface EnableXss {

}
