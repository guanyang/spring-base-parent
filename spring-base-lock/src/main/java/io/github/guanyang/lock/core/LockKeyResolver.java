package io.github.guanyang.lock.core;

import org.aspectj.lang.JoinPoint;
import io.github.guanyang.lock.annotation.Lock;

public interface LockKeyResolver {

    /**
     * 解析一个 Key
     *
     * @param joinPoint  AOP 切面
     * @param annotation 注解
     * @return Key
     */
    String resolver(JoinPoint joinPoint, Lock annotation);
}
