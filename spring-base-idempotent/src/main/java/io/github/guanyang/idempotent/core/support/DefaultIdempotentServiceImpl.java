package io.github.guanyang.idempotent.core.support;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import io.github.guanyang.core.util.CollectionUtils;
import io.github.guanyang.idempotent.annotation.Idempotent;
import io.github.guanyang.idempotent.core.IdempotentKeyResolver;
import io.github.guanyang.idempotent.core.IdempotentService;
import io.github.guanyang.idempotent.exception.IdempotentException;
import io.github.guanyang.idempotent.model.IdempotentContext;
import io.github.guanyang.idempotent.model.IdempotentResult;
import io.github.guanyang.lock.core.DistributedLock;
import io.github.guanyang.lock.core.support.RedisDistributedLock;
import io.github.guanyang.lock.utils.InvokeUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
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
    public <T> IdempotentResult<T> execute(IdempotentContext checkContext, Callable<T> callable) {
        Objects.requireNonNull(checkContext, () -> "checkContext must not be null");
        Objects.requireNonNull(callable, () -> "callable must not be null");
        Objects.requireNonNull(checkContext.getLockService(), () -> "lockService must not be null");
        return internalExecute(checkContext, callable);
    }

    @Override
    public Object invokeFallback(JoinPoint joinPoint, Idempotent annotation, IdempotentException exception) {
        String fallbackMethodName = annotation.fallback();
        Class<?> fallbackBeanClass = annotation.fallbackBean();
        return InvokeUtils.invokeFallback(joinPoint, exception, fallbackMethodName, fallbackBeanClass);
    }

    @SneakyThrows
    protected <T> IdempotentResult<T> internalExecute(IdempotentContext checkContext, Callable<T> callable) {
        boolean lockFlag = false;
        try {
            DistributedLock checkService = checkContext.getLockService();
            lockFlag = checkService.tryLock();
            if (!lockFlag) {
                return IdempotentResult.wrapError();
            }
            T data = callable.call();
            return IdempotentResult.wrapSuccess(data);
        } catch (Throwable ex) {
            if (lockFlag && checkContext.isDeleteKeyWhenException()) {
                Optional.ofNullable(checkContext.getLockService()).ifPresent(DistributedLock::unlock);
            }
            throw ex;
        }
    }
}
