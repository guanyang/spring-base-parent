package org.gy.framework.lock.core.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.lock.annotation.Lock;
import org.gy.framework.lock.core.LockKeyResolver;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class AbstractLockKeyResolver implements LockKeyResolver {

    protected abstract String internalKeyExtractor(JoinPoint joinPoint, Lock annotation);

    @Override
    public String resolver(JoinPoint joinPoint, Lock annotation) {
        return internalResolver(joinPoint, annotation, this::internalKeyExtractor);
    }

    protected String internalResolver(JoinPoint joinPoint, Lock annotation, BiFunction<JoinPoint, Lock, String> keyBuilder) {
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
