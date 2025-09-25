package io.github.guanyang.lock.core;

import org.aspectj.lang.JoinPoint;
import io.github.guanyang.lock.annotation.Lock;
import io.github.guanyang.lock.exception.DistributedLockException;
import io.github.guanyang.lock.model.LockContext;
import io.github.guanyang.lock.model.LockResult;

public interface ILockService {

    /**
     * 创建分布式锁上下文
     */
    LockContext createContext(JoinPoint joinPoint, Lock annotation);

    /**
     * 执行分布式锁
     */
    <T> LockResult<T> execute(LockContext checkContext, DistributedLockCallback<T> runnable);

    /**
     * 执行fallback降级函数
     */
    Object invokeFallback(JoinPoint joinPoint, Lock annotation, DistributedLockException exception);
}
