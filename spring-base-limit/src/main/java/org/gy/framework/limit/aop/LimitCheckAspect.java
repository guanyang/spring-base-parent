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
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.core.ILimitCheckServiceDispatch;
import org.gy.framework.limit.core.support.LimitCheckContext;
import org.gy.framework.limit.enums.LimitTypeEnum;
import org.gy.framework.limit.exception.LimitCodeEnum;
import org.gy.framework.limit.exception.LimitException;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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

    private ExpressionParser parser = new SpelExpressionParser();
    private LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

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

        EvaluationContext context = this.bindParam(method, jp.getArgs());
        String key = this.getValue(context, check.key());
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

    private EvaluationContext bindParam(Method method, Object[] args) {
        //获取方法的参数名
        String[] params = discoverer.getParameterNames(method);
        //将参数名与参数值对应起来
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], args[len]);
        }
        return context;
    }

    private String getValue(EvaluationContext context, String spel) {
        try {
            Object value = parser.parseExpression(spel).getValue(context);
            return String.valueOf(value);
        } catch (Exception e) {
            log.error("[LimitCheckAspect]SPEL analysis error", e);
            throw new LimitException(LimitCodeEnum.PARAM_SPEL_ERROR, e);
        }

    }

}
