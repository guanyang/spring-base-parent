package org.gy.framework.limit.aop.support;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.TypedValue;

/**
 * SPEL表达式解析处理类
 *
 * @author gy
 * @version 1.0.0
 */
public class CustomCachedExpressionEvaluator extends CachedExpressionEvaluator {

    private final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(512);

    private final Map<AnnotatedElementKey, MetaData> metadataCache = new ConcurrentHashMap<>(512);


    public <T> T getValue(Object target, Method method, Object[] args, String expression, Class<T> desiredResultType) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        MetaData metaData = getMetaData(method, targetClass);
        EvaluationContext context = createEvaluationContext(metaData.targetMethod, args);
        return getExpression(expression, metaData.methodKey).getValue(context, desiredResultType);
    }

    protected Expression getExpression(String expression, AnnotatedElementKey methodKey) {
        return getExpression(expressionCache, methodKey, expression);
    }

    protected EvaluationContext createEvaluationContext(Method method, Object[] args) {
        return new MethodBasedEvaluationContext(TypedValue.NULL, method, args, getParameterNameDiscoverer());
    }

    protected MetaData getMetaData(Method method, Class<?> targetClass) {
        AnnotatedElementKey cacheKey = new AnnotatedElementKey(method, targetClass);
        MetaData metaData = metadataCache.get(cacheKey);
        if (metaData == null) {
            metaData = new MetaData(method, targetClass);
            this.metadataCache.put(cacheKey, metaData);
        }
        return metaData;
    }

    protected static class MetaData {

        private final Method method;

        private final Class<?> targetClass;

        private final Method targetMethod;

        private final AnnotatedElementKey methodKey;

        public MetaData(Method method, Class<?> targetClass) {
            this.method = BridgeMethodResolver.findBridgedMethod(method);
            this.targetClass = targetClass;
            this.targetMethod = (!Proxy.isProxyClass(targetClass) ? AopUtils.getMostSpecificMethod(method, targetClass)
                : this.method);
            this.methodKey = new AnnotatedElementKey(this.targetMethod, targetClass);
        }
    }

}
