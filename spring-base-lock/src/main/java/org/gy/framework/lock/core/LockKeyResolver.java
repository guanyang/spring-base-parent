package org.gy.framework.lock.core;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.lock.annotation.Lock;

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
