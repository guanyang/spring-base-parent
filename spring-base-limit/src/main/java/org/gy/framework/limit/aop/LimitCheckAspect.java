package org.gy.framework.limit.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.core.util.CollectionUtils;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.core.ILimitCheckServiceDispatch;
import org.gy.framework.limit.core.LimitKeyResolver;
import org.gy.framework.limit.core.support.LimitCheckContext;
import org.gy.framework.limit.enums.LimitTypeEnum;
import org.gy.framework.limit.exception.LimitCodeEnum;
import org.gy.framework.limit.exception.LimitException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 频率限制切面
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Aspect
public class LimitCheckAspect {

    private final Map<Class<? extends LimitKeyResolver>, LimitKeyResolver> keyResolvers;

    private final ILimitCheckServiceDispatch dispatch;

    public LimitCheckAspect(ILimitCheckServiceDispatch dispatch, List<LimitKeyResolver> keyResolvers) {
        this.dispatch = dispatch;
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, LimitKeyResolver::getClass, Function.identity());
    }

    @Pointcut("@annotation(org.gy.framework.limit.annotation.LimitCheck)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object annotationAround(ProceedingJoinPoint jp) throws Throwable {
        //获取方法
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        String methodName = method.getName();
        // 获取AspectAnnotation注解
        LimitCheck check = method.getAnnotation(LimitCheck.class);
        String type = Optional.ofNullable(check.type()).filter(StringUtils::hasText).orElseGet(LimitTypeEnum.REDIS::getCode);
        ILimitCheckService checkService = dispatch.findService(type);
        Assert.notNull(checkService, () -> "LimitCheck type not support: " + type);

        LimitKeyResolver keyResolver = keyResolvers.get(check.keyResolver());
        Assert.notNull(keyResolver, () -> "LimitKeyResolver not found: " + methodName);
        String key = keyResolver.resolver(jp, check);

        int limit = check.limit();
        long time = check.timeUnit().toMillis(check.time());
        boolean result = checkService.check(LimitCheckContext.of(key, time, limit));
        if (result) {
            log.info("[LimitCheckAspect][{}]方法频率超过阈值，key={},time={}ms,limit={},type={}", methodName, key, time, limit, type);
            throw new LimitException(LimitCodeEnum.EXEC_LIMIT_ERROR, check.message());
        }
        return jp.proceed();
    }

}
