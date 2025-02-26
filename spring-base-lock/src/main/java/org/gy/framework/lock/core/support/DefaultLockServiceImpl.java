package org.gy.framework.lock.core.support;

import cn.hutool.extra.spring.SpringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.core.util.CollectionUtils;
import org.gy.framework.lock.annotation.Lock;
import org.gy.framework.lock.core.*;
import org.gy.framework.lock.exception.DistributedLockException;
import org.gy.framework.lock.model.LockContext;
import org.gy.framework.lock.model.LockResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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
    @SneakyThrows
    public Object invokeFallback(JoinPoint joinPoint, Lock annotation, DistributedLockException exception) {
        String fallbackMethodName = annotation.fallback();
        Class<?> fallbackBeanClass = annotation.fallbackBean();
        Object targetBean = (fallbackBeanClass == Void.class) ? joinPoint.getTarget() : SpringUtil.getBean(fallbackBeanClass);

        // 如果 `fallback` 方法不存在，直接抛出异常
        if (!StringUtils.hasText(fallbackMethodName) || targetBean == null) {
            throw exception;
        }

        Object[] originalArgs = joinPoint.getArgs();
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?>[] parameterTypes = currentMethod.getParameterTypes();
        try {
            //先尝试获取无 `DistributedLockException` 参数的 `fallback` 方法
            Method fallbackMethod = targetBean.getClass().getDeclaredMethod(fallbackMethodName, parameterTypes);
            return fallbackMethod.invoke(targetBean, originalArgs);
        } catch (NoSuchMethodException e) {
            //尝试获取带 `DistributedLockException` 的方法
            Method fallbackMethod = targetBean.getClass().getDeclaredMethod(fallbackMethodName, getParameterTypesWithException(parameterTypes, DistributedLockException.class));
            Object[] fallbackArgs = new Object[originalArgs.length + 1];
            System.arraycopy(originalArgs, 0, fallbackArgs, 0, originalArgs.length);
            fallbackArgs[fallbackArgs.length - 1] = exception;
            return fallbackMethod.invoke(targetBean, fallbackArgs);
        }
    }

    private static Class<?>[] getParameterTypesWithException(Class<?>[] parameterTypes, Class<? extends Throwable> exceptionTypes) {
        Class<?>[] paramTypes = new Class<?>[parameterTypes.length + 1];
        for (int i = 0; i < parameterTypes.length; i++) {
            paramTypes[i] = parameterTypes[i];
        }
        paramTypes[parameterTypes.length] = exceptionTypes;
        return paramTypes;
    }
}
