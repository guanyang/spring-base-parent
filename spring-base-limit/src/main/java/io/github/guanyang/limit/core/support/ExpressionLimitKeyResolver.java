package io.github.guanyang.limit.core.support;

import org.aspectj.lang.JoinPoint;
import io.github.guanyang.limit.annotation.LimitCheck;
import io.github.guanyang.limit.aop.support.CustomCachedExpressionEvaluator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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
        Assert.hasText(annotation.key(), () -> "LimitCheck key must not be empty");
        return evaluator.getValue(joinPoint, annotation.key());
    }
}
