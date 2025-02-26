package org.gy.framework.limit.core.support;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.aop.support.CustomCachedExpressionEvaluator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 基于 Spring EL 表达式
 *
 * @author gy
 */
@Component
public class ExpressionLimitKeyResolver extends AbstractLimitKeyResolver {

    private final CustomCachedExpressionEvaluator evaluator;

    public ExpressionLimitKeyResolver(CustomCachedExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, LimitCheck annotation) {
        return evaluator.getValue(joinPoint, annotation.key());
    }
}
