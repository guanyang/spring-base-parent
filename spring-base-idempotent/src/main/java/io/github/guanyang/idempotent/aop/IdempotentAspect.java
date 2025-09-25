package io.github.guanyang.idempotent.aop;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import io.github.guanyang.idempotent.annotation.Idempotent;
import io.github.guanyang.idempotent.core.IdempotentService;
import io.github.guanyang.idempotent.exception.IdempotentCodeEnum;
import io.github.guanyang.idempotent.exception.IdempotentException;
import io.github.guanyang.idempotent.model.IdempotentContext;
import io.github.guanyang.idempotent.model.IdempotentResult;

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
