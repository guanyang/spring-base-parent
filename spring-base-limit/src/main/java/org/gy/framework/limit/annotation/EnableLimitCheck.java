package org.gy.framework.limit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.gy.framework.limit.LimitCheckConfig;
import org.springframework.context.annotation.Import;

/**
 * 是否开启LimitCheck检查支持
 *
 * @author gy
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LimitCheckConfig.class)
public @interface EnableLimitCheck {

    /**
     * 自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
     */
    String redisTemplateName() default "stringRedisTemplate";

}
