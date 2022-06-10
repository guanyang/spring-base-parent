package org.gy.framework.lock.aop;

import static org.gy.framework.lock.core.DistributedLockAction.execute;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.lock.aop.support.CustomCachedExpressionEvaluator;
import org.gy.framework.lock.core.DistributedLock;
import org.gy.framework.lock.core.support.RedisDistributedLock;
import org.gy.framework.lock.exception.DistributedLockException;
import org.gy.framework.lock.exception.LockCodeEnum;
import org.gy.framework.lock.model.LockResult;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 分布式锁切面
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Aspect
public class DistributedLockAspect {

    private final CustomCachedExpressionEvaluator evaluator = new CustomCachedExpressionEvaluator();

    private StringRedisTemplate stringRedisTemplate;

    public DistributedLockAspect(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Pointcut("@annotation(org.gy.framework.lock.aop.Lock)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object annotationAround(ProceedingJoinPoint jp) throws Throwable {
        //获取方法
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        String methodName = method.getName();
        // 获取AspectAnnotation注解
        Lock lock = method.getAnnotation(Lock.class);
        String key = this.getValue(jp.getTarget(), method, jp.getArgs(), lock.key());
        int expireTime = lock.expireTime();
        long waitTimeMillis = lock.waitTimeMillis();
        long sleepTimeMillis = lock.sleepTimeMillis();
        log.debug("[DistributedLockAspect]{}方法加锁：key={},expireTime={}s,waitTime={}ms,sleepTime={}ms", methodName, key,
            expireTime, waitTimeMillis, sleepTimeMillis);

        //定义redis锁实现
        DistributedLock lockEntity = new RedisDistributedLock(stringRedisTemplate, key, expireTime);

        LockResult<Object> result = execute(lockEntity, waitTimeMillis, sleepTimeMillis, () -> {
            try {
                return jp.proceed();
            } catch (Throwable e) {
                log.error("[DistributedLockAspect]{}内部处理异常，key={},expireTime={}s,waitTime={}ms,sleepTime={}ms.",
                    methodName, key, expireTime, waitTimeMillis, sleepTimeMillis, e);
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new DistributedLockException(LockCodeEnum.INNER_ERROR, e);
            }
        });
        if (result == null || !result.success()) {
            log.warn("[DistributedLockAspect]{}加锁失败，key={},expireTime={}s,waitTime={}ms,sleepTime={}ms.", methodName,
                key, expireTime, waitTimeMillis, sleepTimeMillis);
            throw new DistributedLockException(LockCodeEnum.LOCK_ERROR, "系统繁忙，请稍后重试");
        }

        return result.getData();
    }

    private String getValue(Object target, Method method, Object[] args, String expression) {
        try {
            return evaluator.getValue(target, method, args, expression, String.class);
        } catch (Exception e) {
            log.error("[DistributedLockAspect]SPEL analysis error", e);
            throw new DistributedLockException(LockCodeEnum.PARAM_SPEL_ERROR);
        }

    }


}
