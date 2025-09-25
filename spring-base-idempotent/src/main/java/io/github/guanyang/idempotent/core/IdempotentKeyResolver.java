package io.github.guanyang.idempotent.core;

import org.aspectj.lang.JoinPoint;
import io.github.guanyang.idempotent.annotation.Idempotent;

public interface IdempotentKeyResolver {

    /**
     * 解析一个 Key
     *
     * @param joinPoint  AOP 切面
     * @param idempotent 幂等注解
     * @return Key
     */
    String resolver(JoinPoint joinPoint, Idempotent idempotent);
}
