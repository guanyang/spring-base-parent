package org.gy.framework.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.gy.framework.lock.LockCommon;
import org.springframework.context.annotation.Import;

/**
 * 是否开启LOCK ASPECT支持
 *
 * @author gy
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LockCommon.class)
public @interface EnableLockAspect {

    /**
     * 自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
     */
    String redisTemplateName() default "stringRedisTemplate";

    /**
     * 自定义redissonClient名称，方便切面注入指定bean，解决应用中存在多个redissonClient的问题
     */
    String redissonClientName() default "redisson";

}
