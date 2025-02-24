package org.gy.framework.limit.core.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.core.LimitKeyResolver;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class AbstractLimitKeyResolver implements LimitKeyResolver {

    protected abstract String internalKeyExtractor(JoinPoint joinPoint, LimitCheck annotation);

    @Override
    public String resolver(JoinPoint joinPoint, LimitCheck annotation) {
        return internalResolver(joinPoint, annotation, this::internalKeyExtractor);
    }

    protected String internalResolver(JoinPoint joinPoint, LimitCheck annotation, BiFunction<JoinPoint, LimitCheck, String> keyBuilder) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String keyString = keyBuilder.apply(joinPoint, annotation);
        return StrUtil.join(StrUtil.COLON, annotation.keyPrefix(), method.getName(), keyString);
    }

    protected String paramKeyBuilder(JoinPoint joinPoint, Consumer<StringBuilder> customizer) {
        StringBuilder keyBuilder = new StringBuilder();
        Stream.of(joinPoint.getArgs()).map(Object::toString).forEach(keyBuilder::append);
        Optional.ofNullable(customizer).ifPresent(c -> c.accept(keyBuilder));
        return SecureUtil.md5(keyBuilder.toString());
    }
}
