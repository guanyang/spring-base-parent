package org.gy.framework.lock.aop;

import lombok.SneakyThrows;
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

import java.lang.reflect.Method;

import static org.gy.framework.lock.core.DistributedLockAction.execute;

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

    private final StringRedisTemplate stringRedisTemplate;

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
        long expireTime = lock.expireTimeMillis();
        long waitTimeMillis = lock.waitTimeMillis();
        long sleepTimeMillis = lock.sleepTimeMillis();
        log.info("[DistributedLockAspect][{}]方法加锁：key={},expireTime={}ms,waitTime={}ms,sleepTime={}ms", methodName, key, expireTime, waitTimeMillis, sleepTimeMillis);

        //定义redis锁实现
        DistributedLock lockEntity = new RedisDistributedLock(stringRedisTemplate, key, expireTime);

        LockResult<Object> result = execute(lockEntity, waitTimeMillis, sleepTimeMillis, () -> proceed(jp));
        if (result == null || !result.success()) {
            throw new DistributedLockException(LockCodeEnum.TOO_MANY_REQUESTS);
        }
        return result.getData();
    }

    @SneakyThrows
    protected Object proceed(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }

    private String getValue(Object target, Method method, Object[] args, String expression) {
        try {
            return evaluator.getValue(target, method, args, expression, String.class);
        } catch (Exception e) {
            throw new DistributedLockException(LockCodeEnum.PARAM_SPEL_ERROR, e);
        }

    }


}
