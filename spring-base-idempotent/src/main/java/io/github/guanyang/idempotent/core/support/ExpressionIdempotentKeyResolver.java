package io.github.guanyang.idempotent.core.support;

import org.aspectj.lang.JoinPoint;
import io.github.guanyang.idempotent.annotation.Idempotent;
import io.github.guanyang.lock.aop.support.CustomCachedExpressionEvaluator;
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
