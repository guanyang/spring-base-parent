package org.gy.framework.idempotent.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.idempotent.core.IdempotentKeyResolver;
import org.gy.framework.idempotent.exception.IdempotentCodeEnum;
import org.gy.framework.idempotent.exception.IdempotentException;
import org.gy.framework.idempotent.util.CollectionUtils;
import org.gy.framework.lock.core.DistributedLock;
import org.gy.framework.lock.core.support.RedisDistributedLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 幂等切面
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Aspect
public class IdempotentAspect {

    private final Map<Class<? extends IdempotentKeyResolver>, IdempotentKeyResolver> keyResolvers;

    private final StringRedisTemplate stringRedisTemplate;

    public IdempotentAspect(StringRedisTemplate stringRedisTemplate, List<IdempotentKeyResolver> keyResolvers) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, IdempotentKeyResolver::getClass, Function.identity());
    }

    @Around(value = "@annotation(idempotent)")
    public Object aroundPointCut(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 幂等校验
        DistributedLock lockEntity = internalValidate(joinPoint, idempotent);
        // 方法执行
        return internalProceed(joinPoint, idempotent, lockEntity);
    }

    protected DistributedLock internalValidate(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();

        IdempotentKeyResolver keyResolver = keyResolvers.get(idempotent.keyResolver());
        Assert.notNull(keyResolver, () -> "IdempotentKeyResolver not found: " + methodName);
        String key = keyResolver.resolver(joinPoint, idempotent);

        //定义redis锁实现
        long expireTime = idempotent.timeUnit().toMillis(idempotent.timeout());
        DistributedLock lockEntity = new RedisDistributedLock(stringRedisTemplate, key, expireTime);
        boolean success = lockEntity.tryLock();
        if (!success) {
            log.info("[IdempotentAspect][{}]方法存在重复请求：key={},expireTime={}ms", methodName, key, expireTime);
            throw new IdempotentException(IdempotentCodeEnum.TOO_MANY_REQUESTS, idempotent.message());
        }
        return lockEntity;
    }

    protected Object internalProceed(ProceedingJoinPoint joinPoint, Idempotent idempotent, DistributedLock lockEntity) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            if (idempotent.deleteKeyWhenException()) {
                lockEntity.unlock();
            }
            throw ex;
        }
    }
}
