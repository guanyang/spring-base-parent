package io.github.guanyang.limit.core.support;

import cn.hutool.core.util.ReflectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import io.github.guanyang.core.spi.SpiExtensionFactory;
import io.github.guanyang.core.util.CollectionUtils;
import io.github.guanyang.limit.annotation.LimitCheck;
import io.github.guanyang.limit.aop.support.CustomCachedExpressionEvaluator;
import io.github.guanyang.limit.core.ILimitCheckService;
import io.github.guanyang.limit.core.ILimitCheckServiceDispatch;
import io.github.guanyang.limit.core.LimitKeyResolver;
import io.github.guanyang.limit.enums.LimitTypeEnum;
import io.github.guanyang.limit.exception.LimitException;
import io.github.guanyang.limit.model.LimitCheckContext;
import io.github.guanyang.limit.util.InvokeUtils;
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
