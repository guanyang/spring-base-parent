package org.gy.framework.limit.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.core.ILimitCheckServiceDispatch;
import org.gy.framework.limit.core.support.LimitCheckContext;
import org.gy.framework.limit.exception.LimitCodeEnum;
import org.gy.framework.limit.exception.LimitException;

/**
 * 频率限制切面
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Aspect
public class LimitCheckAspect {

    private final ILimitCheckServiceDispatch dispatch;

    public LimitCheckAspect(ILimitCheckServiceDispatch dispatch) {
        this.dispatch = dispatch;
    }

    @Around(value = "@annotation(annotation)")
    public Object annotationAround(ProceedingJoinPoint jp, LimitCheck annotation) throws Throwable {
        LimitCheckContext checkContext = dispatch.createContext(jp, annotation);
        boolean result = dispatch.check(checkContext);
        if (result) {
            log.info("[LimitCheckAspect]方法频率超过阈值: {}", checkContext);
            LimitException limitException = new LimitException(LimitCodeEnum.EXEC_LIMIT_ERROR, annotation.message(), annotation);
            return dispatch.invokeFallback(jp, annotation, limitException);
        }
        return jp.proceed();
    }

}
