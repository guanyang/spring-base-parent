package org.gy.framework.lock.annotation;

import org.gy.framework.lock.core.LockExecutorResolver;
import org.gy.framework.lock.core.LockKeyResolver;
import org.gy.framework.lock.core.support.ExpressionLockKeyResolver;
import org.gy.framework.lock.core.support.FunctionLockKeyResolver;
import org.gy.framework.lock.core.support.RedissonLockExecutorResolver;
import org.gy.framework.lock.core.support.RedisLockExecutorResolver;

import java.lang.annotation.*;

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
     * 锁的key，支持SpEL或${spring.xxx}
     */
    String key();

    /**
     * Key计算函数
     */
    String keyFunction() default "";

    /**
     * 提示信息
     */
    String message() default "Too Many Requests";

    /**
     * key前缀
     */
    String keyPrefix() default "distributedLock";

    /**
     * 使用的 Key 解析器
     *
     * @see ExpressionLockKeyResolver 自定义表达式，通过 {@link #key()} 计算
     * @see FunctionLockKeyResolver 自定义函数，通过 {@link #keyFunction()} 计算
     */
    Class<? extends LockKeyResolver> keyResolver() default ExpressionLockKeyResolver.class;
    /**
     * 执行器定义
     *
     * @see RedissonLockExecutorResolver 基于Redisson的执行器
     * @see RedisLockExecutorResolver 基于自定义Redis+lua的执行器
     */
    Class<? extends LockExecutorResolver> executorResolver() default RedissonLockExecutorResolver.class;

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
     * 睡眠重试时间，单位：毫秒，支持的LockExecutorResolver：
     * <li>RedisLockExecutorResolver</li>
     */
    long sleepTimeMillis() default 50;

    /**
     * 是否自动续期，默认否，支持的LockExecutorResolver：
     * <li>RedisLockExecutorResolver</li>
     */
    boolean renewal() default false;

    /**
     * 自定义降级方法，不定义则抛出LimitException
     */
    String fallback() default "";

    /**
     * 降级方法所在的 Spring Bean，默认为当前类
     */
    Class<?> fallbackBean() default Void.class;

}
