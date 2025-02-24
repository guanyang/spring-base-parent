package org.gy.framework.idempotent.core.support;

import cn.hutool.core.util.StrUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.idempotent.core.IdempotentKeyResolver;
import org.gy.framework.idempotent.exception.IdempotentCodeEnum;
import org.gy.framework.idempotent.exception.IdempotentException;
import org.gy.framework.lock.aop.support.CustomCachedExpressionEvaluator;

import java.lang.reflect.Method;

public class ExpressionIdempotentKeyResolver extends AbstractIdempotentKeyResolver {

    private final CustomCachedExpressionEvaluator evaluator = new CustomCachedExpressionEvaluator();

    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, Idempotent idempotent) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return this.getValue(joinPoint.getTarget(), method, joinPoint.getArgs(), idempotent.keyArg());
    }

    private String getValue(Object target, Method method, Object[] args, String expression) {
        try {
            return evaluator.getValue(target, method, args, expression, String.class);
        } catch (Exception e) {
            throw new IdempotentException(IdempotentCodeEnum.PARAM_SPEL_ERROR, e);
        }
    }
}
