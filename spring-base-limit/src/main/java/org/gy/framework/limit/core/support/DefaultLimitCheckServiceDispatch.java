package org.gy.framework.limit.core.support;

import cn.hutool.core.util.ReflectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.core.spi.SpiExtensionFactory;
import org.gy.framework.core.util.CollectionUtils;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.aop.support.CustomCachedExpressionEvaluator;
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.core.ILimitCheckServiceDispatch;
import org.gy.framework.limit.core.LimitKeyResolver;
import org.gy.framework.limit.enums.LimitTypeEnum;
import org.gy.framework.limit.exception.LimitException;
import org.gy.framework.limit.model.LimitCheckContext;
import org.gy.framework.limit.util.InvokeUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 频率限制检查服务工厂
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class DefaultLimitCheckServiceDispatch implements ILimitCheckServiceDispatch {

    private final Map<Class<? extends LimitKeyResolver>, LimitKeyResolver> keyResolvers;

    private final CustomCachedExpressionEvaluator evaluator;
    /**
     * redis客户端
     */
    private final StringRedisTemplate stringRedisTemplate;

    public DefaultLimitCheckServiceDispatch(List<LimitKeyResolver> keyResolvers, CustomCachedExpressionEvaluator evaluator) {
        this(keyResolvers, evaluator, null);
    }

    public DefaultLimitCheckServiceDispatch(List<LimitKeyResolver> keyResolvers, CustomCachedExpressionEvaluator evaluator, StringRedisTemplate stringRedisTemplate) {
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, LimitKeyResolver::getClass, Function.identity());
        this.evaluator = evaluator;
        this.stringRedisTemplate = stringRedisTemplate;
        initLimitCheck(stringRedisTemplate);
    }

    public static void initLimitCheck(StringRedisTemplate stringRedisTemplate) {
        if (stringRedisTemplate == null) {
            return;
        }
        Arrays.stream(LimitTypeEnum.values()).map(LimitTypeEnum::getCheckClass).filter(Objects::nonNull).forEach(item -> {
            ILimitCheckService checkService = ReflectUtil.newInstance(item, stringRedisTemplate);
            addLimitCheckIfAbsent(checkService);
        });
    }

    public static void addLimitCheckIfAbsent(ILimitCheckService service) {
        SpiExtensionFactory.addExtensionIfAbsent(ILimitCheckService.class, service);
    }

    @Override
    public LimitCheckContext createContext(JoinPoint joinPoint, LimitCheck check) {
        //获取方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();

        String type = Optional.ofNullable(check.type()).filter(StringUtils::hasText).orElseGet(() -> check.typeEnum().getCode());
        ILimitCheckService checkService = findService(type);
        Assert.notNull(checkService, () -> "LimitCheck type not support: " + type);

        LimitKeyResolver keyResolver = keyResolvers.get(check.keyResolver());
        Assert.notNull(keyResolver, () -> "LimitKeyResolver not found: " + methodName);
        String key = keyResolver.resolver(joinPoint, check);

        int limit = getValue(joinPoint, check.limitExpression(), Integer::parseInt, check::limit);
        long time = check.timeUnit().toMillis(check.time());
        LimitCheckContext checkContext = LimitCheckContext.of(key, time, limit);
        checkContext.setType(type);
        checkContext.setMessage(check.message());
        int capacity = getValue(joinPoint, check.capacityExpression(), Integer::parseInt, check::capacity);
        checkContext.setCapacity(capacity);
        checkContext.setRequested(check.requested());
        checkContext.setCheckService(checkService);
        checkContext.setMethodName(methodName);
        return checkContext;
    }

    @Override
    public boolean check(LimitCheckContext checkContext) {
        Objects.requireNonNull(checkContext, () -> "checkContext must not be null");
        ILimitCheckService checkService = Objects.requireNonNull(checkContext.getCheckService(), () -> "checkService must not be null");
        return checkService.check(checkContext);
    }

    @Override
    @SneakyThrows
    public Object invokeFallback(JoinPoint joinPoint, LimitCheck limitCheck, LimitException limitException) {
        String fallbackMethodName = limitCheck.fallback();
        Class<?> fallbackBeanClass = limitCheck.fallbackBean();
        return InvokeUtils.invokeFallback(joinPoint, limitException, fallbackMethodName, fallbackBeanClass);
    }

    private <T> T getValue(JoinPoint joinPoint, String expression, Function<String, T> function, Supplier<T> defaultValue) {
        return Optional.ofNullable(expression).filter(StringUtils::hasText).map(s -> evaluator.getValue(joinPoint, s)).map(function).orElseGet(defaultValue);
    }
}
