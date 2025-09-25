package io.github.guanyang.lock.core.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import io.github.guanyang.lock.annotation.Lock;
import io.github.guanyang.lock.core.LockKeyResolver;

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
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodKey = getMethodKey(signature);
        String keyString = keyBuilder.apply(joinPoint, annotation);
        return StrUtil.join(StrUtil.COLON, annotation.keyPrefix(), methodKey, keyString);
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
