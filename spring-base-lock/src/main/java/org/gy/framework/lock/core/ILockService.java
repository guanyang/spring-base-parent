package org.gy.framework.lock.core;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.lock.annotation.Lock;
import org.gy.framework.lock.exception.DistributedLockException;
import org.gy.framework.lock.model.LockContext;
import org.gy.framework.lock.model.LockResult;

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
