package io.github.guanyang.idempotent.annotation;

import io.github.guanyang.idempotent.IdempotentConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(IdempotentConfig.class)
public @interface EnableIdempotent {

    @Deprecated
    String DEFAULT_NAME = "stringRedisTemplate";

    String REDISSON_CLIENT_NAME = "redisson";

    /**
     * 自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
     */
    @Deprecated
    String redisTemplateName() default DEFAULT_NAME;

    /**
     * 自定义redissonClient名称，方便切面注入指定bean，解决应用中存在多个redissonClient的问题
     */
    String redissonClientName() default REDISSON_CLIENT_NAME;
}
