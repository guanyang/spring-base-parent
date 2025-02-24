package org.gy.framework.limit.core;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.limit.annotation.LimitCheck;

public interface LimitKeyResolver {

    /**
     * 解析一个 Key
     *
     * @param joinPoint  AOP 切面
     * @param annotation 注解
     * @return Key
     */
    String resolver(JoinPoint joinPoint, LimitCheck annotation);
}
