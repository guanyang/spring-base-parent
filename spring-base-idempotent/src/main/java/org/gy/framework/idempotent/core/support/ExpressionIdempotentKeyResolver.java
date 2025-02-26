package org.gy.framework.idempotent.core.support;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.lock.aop.support.CustomCachedExpressionEvaluator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ExpressionIdempotentKeyResolver extends AbstractIdempotentKeyResolver {

    private final CustomCachedExpressionEvaluator evaluator;

    public ExpressionIdempotentKeyResolver(CustomCachedExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, Idempotent idempotent) {
        Assert.hasText(idempotent.key(), () -> "Idempotent key must not be empty");
        return evaluator.getValue(joinPoint, idempotent.key());
    }
}
