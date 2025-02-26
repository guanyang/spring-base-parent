package org.gy.framework.idempotent.core;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.idempotent.exception.IdempotentException;
import org.gy.framework.idempotent.model.IdempotentContext;

public interface IdempotentService {

    /**
     * 创建上下文
     */
    IdempotentContext createContext(JoinPoint joinPoint, Idempotent annotation);

    /**
     * 检查
     */
    boolean check(IdempotentContext checkContext);

    /**
     * 执行fallback降级函数
     */
    Object invokeFallback(JoinPoint joinPoint, Idempotent annotation, IdempotentException exception);
}
