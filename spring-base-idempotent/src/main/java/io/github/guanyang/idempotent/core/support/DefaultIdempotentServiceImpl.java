package io.github.guanyang.idempotent.core.support;

import io.github.guanyang.core.util.CollectionUtils;
import io.github.guanyang.idempotent.annotation.Idempotent;
import io.github.guanyang.idempotent.core.IdempotentKeyResolver;
import io.github.guanyang.idempotent.core.IdempotentService;
import io.github.guanyang.idempotent.exception.IdempotentException;
import io.github.guanyang.idempotent.model.IdempotentContext;
import io.github.guanyang.idempotent.model.IdempotentResult;
import io.github.guanyang.lock.aop.support.CustomCachedExpressionEvaluator;
import io.github.guanyang.lock.core.DistributedLock;
import io.github.guanyang.lock.core.support.RedisDistributedLock;
import io.github.guanyang.lock.core.support.RedissonDistributedLock;
import io.github.guanyang.lock.utils.InvokeUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class DefaultIdempotentServiceImpl implements IdempotentService {

    private final Map<Class<? extends IdempotentKeyResolver>, IdempotentKeyResolver> keyResolvers;

    @Deprecated
    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final CustomCachedExpressionEvaluator evaluator;

    public DefaultIdempotentServiceImpl(List<IdempotentKeyResolver> keyResolvers, StringRedisTemplate stringRedisTemplate, CustomCachedExpressionEvaluator evaluator) {
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, IdempotentKeyResolver::getClass, Function.identity());
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = null;
        this.evaluator = evaluator;
    }

    public DefaultIdempotentServiceImpl(List<IdempotentKeyResolver> keyResolvers, RedissonClient redissonClient, CustomCachedExpressionEvaluator evaluator) {
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, IdempotentKeyResolver::getClass, Function.identity());
        this.redissonClient = redissonClient;
        this.stringRedisTemplate = null;
        this.evaluator = evaluator;
    }

    @Override
    public IdempotentContext createContext(JoinPoint joinPoint, Idempotent annotation) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();

        IdempotentKeyResolver keyResolver = keyResolvers.get(annotation.keyResolver());
        Assert.notNull(keyResolver, () -> "IdempotentKeyResolver not found: " + methodName);
        String key = keyResolver.resolver(joinPoint, annotation);

        return buildContext(joinPoint, annotation, key, methodName);
    }

    protected IdempotentContext buildContext(JoinPoint joinPoint, Idempotent annotation, String key, String methodName) {
        IdempotentContext context = new IdempotentContext();
        context.setKey(key);
        context.setMethodName(methodName);
        context.setMessage(annotation.message());
        int timeout = getValue(joinPoint, annotation.timeoutExpression(), Integer::parseInt, annotation::timeout);
        context.setTimeout(timeout);
        context.setTimeUnit(annotation.timeUnit());
        context.setDeleteKeyWhenException(annotation.deleteKeyWhenException());
        context.setAnnotation(annotation);

        //定义redis锁实现
        long expireTime = annotation.timeUnit().toMillis(annotation.timeout());
        DistributedLock lockEntity = buildLockService(key, expireTime);
        context.setLockService(lockEntity);
        return context;
    }

    protected DistributedLock buildLockService(String lockKey, long expireMillis) {
        if (redissonClient != null) {
            return new RedissonDistributedLock(redissonClient, lockKey, expireMillis);
        } else if (stringRedisTemplate != null) {
            return new RedisDistributedLock(stringRedisTemplate, lockKey, expireMillis);
        } else {
            throw new IllegalArgumentException("redisTemplate must not be null");
        }
    }

    protected <T> T getValue(JoinPoint joinPoint, String expression, Function<String, T> function, Supplier<T> defaultValue) {
        return Optional.ofNullable(expression).filter(StringUtils::hasText).map(s -> evaluator.getValue(joinPoint, s)).map(function).orElseGet(defaultValue);
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
