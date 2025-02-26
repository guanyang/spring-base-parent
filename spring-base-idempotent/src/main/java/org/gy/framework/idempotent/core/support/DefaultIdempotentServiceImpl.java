package org.gy.framework.idempotent.core.support;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.core.util.CollectionUtils;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.idempotent.core.IdempotentKeyResolver;
import org.gy.framework.idempotent.core.IdempotentService;
import org.gy.framework.idempotent.exception.IdempotentException;
import org.gy.framework.idempotent.model.IdempotentContext;
import org.gy.framework.lock.core.DistributedLock;
import org.gy.framework.lock.core.support.RedisDistributedLock;
import org.gy.framework.lock.utils.InvokeUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class DefaultIdempotentServiceImpl implements IdempotentService {

    private final Map<Class<? extends IdempotentKeyResolver>, IdempotentKeyResolver> keyResolvers;

    private final StringRedisTemplate stringRedisTemplate;

    public DefaultIdempotentServiceImpl(List<IdempotentKeyResolver> keyResolvers, StringRedisTemplate stringRedisTemplate) {
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, IdempotentKeyResolver::getClass, Function.identity());
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public IdempotentContext createContext(JoinPoint joinPoint, Idempotent annotation) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();

        IdempotentKeyResolver keyResolver = keyResolvers.get(annotation.keyResolver());
        Assert.notNull(keyResolver, () -> "IdempotentKeyResolver not found: " + methodName);
        String key = keyResolver.resolver(joinPoint, annotation);

        return buildContext(annotation, key, methodName);
    }

    protected IdempotentContext buildContext(Idempotent annotation, String key, String methodName) {
        IdempotentContext context = new IdempotentContext();
        context.setKey(key);
        context.setMethodName(methodName);
        context.setMessage(annotation.message());
        context.setTimeout(annotation.timeout());
        context.setTimeUnit(annotation.timeUnit());
        context.setDeleteKeyWhenException(annotation.deleteKeyWhenException());
        context.setAnnotation(annotation);

        //定义redis锁实现
        long expireTime = annotation.timeUnit().toMillis(annotation.timeout());
        DistributedLock lockEntity = new RedisDistributedLock(stringRedisTemplate, key, expireTime);
        context.setLockService(lockEntity);
        return context;
    }

    @Override
    public boolean check(IdempotentContext checkContext) {
        Objects.requireNonNull(checkContext, () -> "checkContext must not be null");
        DistributedLock checkService = Objects.requireNonNull(checkContext.getLockService(), () -> "lockService must not be null");
        return checkService.tryLock();
    }

    @Override
    public Object invokeFallback(JoinPoint joinPoint, Idempotent annotation, IdempotentException exception) {
        String fallbackMethodName = annotation.fallback();
        Class<?> fallbackBeanClass = annotation.fallbackBean();
        return InvokeUtils.invokeFallback(joinPoint, exception, fallbackMethodName, fallbackBeanClass);
    }
}
