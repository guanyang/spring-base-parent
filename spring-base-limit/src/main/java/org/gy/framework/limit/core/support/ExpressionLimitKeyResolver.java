package org.gy.framework.limit.core.support;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.aop.support.CustomCachedExpressionEvaluator;
import org.gy.framework.limit.exception.LimitCodeEnum;
import org.gy.framework.limit.exception.LimitException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 基于 Spring EL 表达式
 *
 * @author gy
 */
@Component
public class ExpressionLimitKeyResolver extends AbstractLimitKeyResolver {

    private final CustomCachedExpressionEvaluator evaluator = new CustomCachedExpressionEvaluator();

    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, LimitCheck annotation) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return this.getValue(joinPoint.getTarget(), method, joinPoint.getArgs(), annotation.key());
    }

    private String getValue(Object target, Method method, Object[] args, String expression) {
        try {
            return evaluator.getValue(target, method, args, expression, String.class);
        } catch (Exception e) {
            throw new LimitException(LimitCodeEnum.PARAM_SPEL_ERROR, e);
        }
    }
}
