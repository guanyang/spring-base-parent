package org.gy.framework.lock.core.support;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.core.util.CollectionUtils;
import org.gy.framework.lock.annotation.Lock;
import org.gy.framework.lock.core.*;
import org.gy.framework.lock.exception.DistributedLockException;
import org.gy.framework.lock.model.LockContext;
import org.gy.framework.lock.model.LockResult;
import org.gy.framework.lock.utils.InvokeUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class DefaultLockServiceImpl implements ILockService {

    private final Map<Class<? extends LockKeyResolver>, LockKeyResolver> keyResolvers;
    /**
     * redis客户端
     */
    private final StringRedisTemplate stringRedisTemplate;

    public DefaultLockServiceImpl(List<LockKeyResolver> keyResolvers, StringRedisTemplate stringRedisTemplate) {
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, LockKeyResolver::getClass, Function.identity());
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public LockContext createContext(JoinPoint joinPoint, Lock annotation) {
        //获取方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();

        LockKeyResolver keyResolver = keyResolvers.get(annotation.keyResolver());
        Assert.notNull(keyResolver, () -> "LockKeyResolver not found: " + methodName);
        String key = keyResolver.resolver(joinPoint, annotation);

        return buildLockContext(annotation, key, methodName);
    }

    protected LockContext buildLockContext(Lock annotation, String key, String methodName) {
        LockContext context = new LockContext();
        context.setKey(key);
        context.setMethodName(methodName);
        context.setMessage(annotation.message());
        context.setExpireTimeMillis(annotation.expireTimeMillis());
        context.setWaitTimeMillis(annotation.waitTimeMillis());
        context.setSleepTimeMillis(annotation.sleepTimeMillis());
        context.setRenewal(annotation.renewal());
        context.setAnnotation(annotation);

        DistributedLock lockEntity = new RedisDistributedLock(stringRedisTemplate, key, annotation.expireTimeMillis(), annotation.renewal());
        context.setLockService(lockEntity);
        return context;
    }

    @Override
    public <T> LockResult<T> execute(LockContext checkContext, DistributedLockCallback<T> runnable) {
        Objects.requireNonNull(checkContext, () -> "checkContext must not be null");
        Objects.requireNonNull(runnable, () -> "runnable must not be null");
        DistributedLock checkService = Objects.requireNonNull(checkContext.getLockService(), () -> "lockService must not be null");
        return DistributedLockAction.execute(checkService, checkContext.getWaitTimeMillis(), checkContext.getSleepTimeMillis(), runnable);
    }

    @Override
    public Object invokeFallback(JoinPoint joinPoint, Lock annotation, DistributedLockException exception) {
        String fallbackMethodName = annotation.fallback();
        Class<?> fallbackBeanClass = annotation.fallbackBean();
        return InvokeUtils.invokeFallback(joinPoint, exception, fallbackMethodName, fallbackBeanClass);
    }
}
