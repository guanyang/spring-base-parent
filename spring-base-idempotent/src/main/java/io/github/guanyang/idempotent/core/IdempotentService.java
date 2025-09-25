package io.github.guanyang.idempotent.core;

import org.aspectj.lang.JoinPoint;
import io.github.guanyang.idempotent.annotation.Idempotent;
import io.github.guanyang.idempotent.exception.IdempotentException;
import io.github.guanyang.idempotent.model.IdempotentContext;
import io.github.guanyang.idempotent.model.IdempotentResult;

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
