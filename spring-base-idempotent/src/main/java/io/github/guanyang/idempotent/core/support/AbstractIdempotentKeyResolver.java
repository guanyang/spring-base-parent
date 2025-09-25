package io.github.guanyang.idempotent.core.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import io.github.guanyang.idempotent.annotation.Idempotent;
import io.github.guanyang.idempotent.core.IdempotentKeyResolver;

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
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodKey = getMethodKey(signature);
        String keyString = keyBuilder.apply(joinPoint, idempotent);
        return StrUtil.join(StrUtil.COLON, idempotent.keyPrefix(), methodKey, keyString);
    }

    protected String getMethodKey(MethodSignature signature) {
        //基于类名和方法名，确保唯一，md5处理，减少key长度
        String methodKey = StrUtil.join(StrUtil.DOT, signature.getDeclaringTypeName(), signature.getMethod().getName());
        return SecureUtil.md5(methodKey);
    }

    protected String paramKeyBuilder(JoinPoint joinPoint, Consumer<StringBuilder> customizer) {
        StringBuilder keyBuilder = new StringBuilder();
        Stream.of(joinPoint.getArgs()).map(Object::toString).forEach(keyBuilder::append);
        Optional.ofNullable(customizer).ifPresent(c -> c.accept(keyBuilder));
        return SecureUtil.md5(keyBuilder.toString());
    }
}
