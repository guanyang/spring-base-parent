package org.gy.framework.idempotent.aop;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.idempotent.core.IdempotentService;
import org.gy.framework.idempotent.exception.IdempotentCodeEnum;
import org.gy.framework.idempotent.exception.IdempotentException;
import org.gy.framework.idempotent.model.IdempotentContext;
import org.gy.framework.idempotent.model.IdempotentResult;
import org.gy.framework.lock.core.DistributedLock;

import java.util.Optional;

/**
 * 幂等切面
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Aspect
public class IdempotentAspect {

    private final IdempotentService idempotentService;

    public IdempotentAspect(IdempotentService idempotentService) {
        this.idempotentService = idempotentService;
    }

    @Around(value = "@annotation(annotation)")
    public Object aroundPointCut(ProceedingJoinPoint joinPoint, Idempotent annotation) throws Throwable {
        IdempotentContext checkContext = idempotentService.createContext(joinPoint, annotation);
        IdempotentResult<Object> result = idempotentService.execute(checkContext, () -> proceed(joinPoint));
        if (result == null || !result.success()) {
            log.info("[IdempotentAspect]方法存在重复请求：{}", checkContext);
            IdempotentException exception = new IdempotentException(IdempotentCodeEnum.TOO_MANY_REQUESTS, annotation.message(), annotation);
            return idempotentService.invokeFallback(joinPoint, annotation, exception);
        }
        return result.getData();
    }

    @SneakyThrows
    protected Object proceed(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }
}
