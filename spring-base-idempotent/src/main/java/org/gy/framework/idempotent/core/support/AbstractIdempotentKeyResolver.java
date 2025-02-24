package org.gy.framework.idempotent.core.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.idempotent.core.IdempotentKeyResolver;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class AbstractIdempotentKeyResolver implements IdempotentKeyResolver {

    protected abstract String internalKeyExtractor(JoinPoint joinPoint, Idempotent idempotent);

    @Override
    public String resolver(JoinPoint joinPoint, Idempotent idempotent) {
        return internalResolver(joinPoint, idempotent, this::internalKeyExtractor);
    }

    protected String internalResolver(JoinPoint joinPoint, Idempotent idempotent, BiFunction<JoinPoint, Idempotent, String> keyBuilder) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String keyString = keyBuilder.apply(joinPoint, idempotent);
        return StrUtil.join(StrUtil.COLON, idempotent.keyPrefix(), method.getName(), keyString);
    }

    protected String paramKeyBuilder(JoinPoint joinPoint, Consumer<StringBuilder> customizer) {
        StringBuilder keyBuilder = new StringBuilder();
        Stream.of(joinPoint.getArgs()).map(Object::toString).forEach(keyBuilder::append);
        Optional.ofNullable(customizer).ifPresent(c -> c.accept(keyBuilder));
        return SecureUtil.md5(keyBuilder.toString());
    }
}
