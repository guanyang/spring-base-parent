package org.gy.framework.idempotent.core;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.idempotent.exception.IdempotentException;
import org.gy.framework.idempotent.model.IdempotentContext;
import org.gy.framework.idempotent.model.IdempotentResult;

import java.util.concurrent.Callable;

public interface IdempotentService {

    /**
     * 创建上下文
     */
    IdempotentContext createContext(JoinPoint joinPoint, Idempotent annotation);

    /**
     * 幂等执行
     */
    <T> IdempotentResult<T> execute(IdempotentContext checkContext, Callable<T> callable);

    /**
     * 执行fallback降级函数
     */
    Object invokeFallback(JoinPoint joinPoint, Idempotent annotation, IdempotentException exception);
}
