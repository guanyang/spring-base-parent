package org.gy.framework.idempotent.core.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.idempotent.core.IdempotentKeyResolver;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * 默认（全局级别）幂等 Key 解析器
 *
 * @author gy
 */
public class DefaultIdempotentKeyResolver implements IdempotentKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, Idempotent idempotent) {
        StringBuilder keyBuilder = new StringBuilder();
        Stream.of(joinPoint.getArgs()).map(Object::toString).forEach(keyBuilder::append);
        String md5 = SecureUtil.md5(keyBuilder.toString());
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return StrUtil.join(StrUtil.COLON, idempotent.keyPrefix(), method.getName(), md5);
    }

}
