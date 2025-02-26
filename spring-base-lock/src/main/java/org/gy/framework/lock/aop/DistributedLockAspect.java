package org.gy.framework.lock.aop;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.gy.framework.lock.annotation.Lock;
import org.gy.framework.lock.core.ILockService;
import org.gy.framework.lock.exception.DistributedLockException;
import org.gy.framework.lock.exception.LockCodeEnum;
import org.gy.framework.lock.model.LockContext;
import org.gy.framework.lock.model.LockResult;

/**
 * 分布式锁切面
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Aspect
public class DistributedLockAspect {

    private final ILockService lockService;

    public DistributedLockAspect(ILockService lockService) {
        this.lockService = lockService;
    }

    @Around(value = "@annotation(annotation)")
    public Object annotationAround(ProceedingJoinPoint jp, Lock annotation) throws Throwable {
        LockContext context = lockService.createContext(jp, annotation);
        log.info("[DistributedLockAspect]方法加锁：{}", context);

        LockResult<Object> result = lockService.execute(context, () -> proceed(jp));
        if (result == null || !result.success()) {
            DistributedLockException lockException = new DistributedLockException(LockCodeEnum.TOO_MANY_REQUESTS, annotation.message(), annotation);
            return lockService.invokeFallback(jp, annotation, lockException);
        }
        return result.getData();
    }

    @SneakyThrows
    protected Object proceed(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }

}
