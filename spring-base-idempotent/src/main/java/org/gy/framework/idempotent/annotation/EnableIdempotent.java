package org.gy.framework.idempotent.annotation;

import org.gy.framework.idempotent.IdempotentConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(IdempotentConfig.class)
public @interface EnableIdempotent {

    String DEFAULT_NAME = "stringRedisTemplate";

    /**
     * 自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
     */
    String redisTemplateName() default DEFAULT_NAME;
}
