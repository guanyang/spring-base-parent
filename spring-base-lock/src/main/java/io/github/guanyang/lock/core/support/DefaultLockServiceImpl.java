package io.github.guanyang.lock.core.support;

import io.github.guanyang.core.util.CollectionUtils;
import io.github.guanyang.lock.annotation.Lock;
import io.github.guanyang.lock.core.*;
import io.github.guanyang.lock.exception.DistributedLockException;
import io.github.guanyang.lock.model.LockContext;
import io.github.guanyang.lock.model.LockEntry;
import io.github.guanyang.lock.model.LockResult;
import io.github.guanyang.lock.utils.InvokeUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class DefaultLockServiceImpl implements ILockService {

    private final Map<Class<? extends LockKeyResolver>, LockKeyResolver> keyResolvers;

    private final Map<Class<? extends LockExecutorResolver>, LockExecutorResolver> lockExecutorResolvers;

    public DefaultLockServiceImpl(List<LockKeyResolver> keyResolvers, List<LockExecutorResolver> lockExecutorResolvers) {
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, LockKeyResolver::getClass, Function.identity());
        this.lockExecutorResolvers = CollectionUtils.convertMap(lockExecutorResolvers, LockExecutorResolver::getClass, Function.identity());
    }

    @Override
    public LockContext createContext(JoinPoint joinPoint, Lock annotation) {
        //获取方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();

        LockKeyResolver keyResolver = keyResolvers.get(annotation.keyResolver());
        Assert.notNull(keyResolver, () -> "LockKeyResolver not found: " + methodName);

        LockExecutorResolver executorResolver = lockExecutorResolvers.get(annotation.executorResolver());
        Assert.notNull(executorResolver, () -> "LockExecutorResolver not found: " + methodName);

        String key = keyResolver.resolver(joinPoint, annotation);
        LockEntry lockEntry = LockEntry.builder().lockKey(key).expireMillis(annotation.expireTimeMillis()).renewal(annotation.renewal()).build();
        DistributedLock distributedLock = executorResolver.resolve(lockEntry);

        LockContext lockContext = buildLockContext(annotation, key, methodName);
        lockContext.setLockService(distributedLock);
        return lockContext;
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
