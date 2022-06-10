package org.gy.framework.limit.aop;

import java.lang.reflect.Method;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.aop.support.CustomCachedExpressionEvaluator;
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.core.ILimitCheckServiceDispatch;
import org.gy.framework.limit.core.support.LimitCheckContext;
import org.gy.framework.limit.enums.LimitTypeEnum;
import org.gy.framework.limit.exception.LimitCodeEnum;
import org.gy.framework.limit.exception.LimitException;
import org.springframework.util.StringUtils;

/**
 * 频率限制切面
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Aspect
public class LimitCheckAspect {

    private final CustomCachedExpressionEvaluator evaluator = new CustomCachedExpressionEvaluator();

    private ILimitCheckServiceDispatch dispatch;

    public LimitCheckAspect(ILimitCheckServiceDispatch dispatch) {
        this.dispatch = dispatch;
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
        String type = Optional.ofNullable(check.type()).filter(StringUtils::hasText)
            .orElseGet(LimitTypeEnum.REDIS::getCode);
        ILimitCheckService checkService = dispatch.findService(type);
        if (checkService == null) {
            log.error("[LimitCheckAspect]{}频率检查类型不支持：type={}.", methodName, type);
            throw new LimitException(LimitCodeEnum.INNER_ERROR);
        }
        String key = this.getValue(jp.getTarget(), method, jp.getArgs(), check.key());
        int time = check.time();
        int limit = check.limit();
        log.debug("[LimitCheckAspect]{}方法频率检查：key={},time={}S,limit={},type={}", methodName, key, time, limit, type);
        boolean result = checkService.check(LimitCheckContext.of(key, time, limit));
        if (result) {
            log.info("[LimitCheckAspect]{}方法频率超过阈值，key={},time={}S,limit={}", methodName, key, time, limit);
            throw new LimitException(LimitCodeEnum.EXEC_LIMIT_ERROR);
        }
        return jp.proceed();
    }

    private String getValue(Object target, Method method, Object[] args, String expression) {
        try {
            return evaluator.getValue(target, method, args, expression, String.class);
        } catch (Exception e) {
            log.error("[LimitCheckAspect]SPEL analysis error", e);
            throw new LimitException(LimitCodeEnum.PARAM_SPEL_ERROR, e);
        }

    }

}
