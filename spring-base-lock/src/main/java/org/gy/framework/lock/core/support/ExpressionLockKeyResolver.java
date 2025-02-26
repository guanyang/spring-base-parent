package org.gy.framework.lock.core.support;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.lock.annotation.Lock;
import org.gy.framework.lock.aop.support.CustomCachedExpressionEvaluator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 基于 Spring EL 表达式
 *
 * @author gy
 */
@Component
public class ExpressionLockKeyResolver extends AbstractLockKeyResolver {

    private final CustomCachedExpressionEvaluator evaluator;

    public ExpressionLockKeyResolver(CustomCachedExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, Lock annotation) {
        Assert.hasText(annotation.key(), () -> "Lock key must not be empty");
        return evaluator.getValue(joinPoint, annotation.key());
    }
}
