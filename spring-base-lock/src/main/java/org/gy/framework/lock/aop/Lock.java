package org.gy.framework.lock.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁注解
 *
 * @author gy
 * @version 1.0.0
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {

    /**
     * 锁的key
     */
    String key();

    /**
     * 锁过期时间，单位：毫秒
     */
    int expireTimeMillis() default 30000;

    /**
     * 等待超时时间，单位：毫秒
     * <li>waitTimeMillis=0，仅尝试一次获取锁<li/>
     * <li>waitTimeMillis=-1，一直尝试获取锁直到成功<li/>
     * <li>waitTimeMillis>0，自定义阻塞时间，超时则获取锁失败<li/>
     */
    long waitTimeMillis() default 0;

    /**
     * 睡眠重试时间，单位：毫秒
     */
    long sleepTimeMillis() default 30;

}
