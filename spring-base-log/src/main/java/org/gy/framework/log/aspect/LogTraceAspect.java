package org.gy.framework.log.aspect;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.log.annotation.LogTrace;
import org.gy.framework.log.util.LogTraceUtil;
import org.springframework.stereotype.Component;

/**
 * 功能描述：日志输出切面
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
public class LogTraceAspect {

    @Pointcut("@within(org.gy.framework.log.annotation.LogTrace) || @annotation(org.gy.framework.log.annotation.LogTrace)")
    public void pointcutName() {

    }

    @Around("pointcutName()")
    public Object log(ProceedingJoinPoint point) throws Throwable {
        Object result = null;
        try {
            // 前置处理
            preHandle(point);
            result = point.proceed();
            // 后置处理
            postHandle(result);
        } catch (Exception e) {
            log.error("[LogTraceAspect]proceed exception.", e);
            postHandle(e);
            throw e;
        }
        return result;

    }

    private void preHandle(ProceedingJoinPoint point) {
        try {
            // 拦截的实体类
            Object target = point.getTarget();
            // 方法签名
            MethodSignature signature = (MethodSignature) point.getSignature();
            // 拦截的方法名称
            String methodName = signature.getName();

            // 获取方法
            Method method = signature.getMethod();
            // 获取LogAnnotation
            LogTrace annotation = method.getAnnotation(LogTrace.class);
            // 获取描述
            String desc = Optional.ofNullable(annotation).map(LogTrace::desc).orElse("default");

            Object requestObj = wrapRequestBody(point, signature, annotation);

            LogTraceUtil.preTrace(target.getClass(), methodName, requestObj, desc);
        } catch (Exception e) {
            log.warn("[LogTraceAspect]preHandle Exception.", e);
        }

    }

    private void postHandle(Object responseObj) {
        LogTraceUtil.postTrace(responseObj);
    }

    private void postHandle(Exception e) {
        LogTraceUtil.postTrace(e);
    }

    /**
     * 功能描述:
     *
     * @author gy
     */
    private Object wrapRequestBody(ProceedingJoinPoint point,
        MethodSignature signature, LogTrace annotation) {
        Map<String, Object> paramNameAndValue = Maps.newHashMap();
        // 参数值
        Object[] args = point.getArgs();
        if (args == null || args.length == 0) {
            return paramNameAndValue;
        }
        // 指定参数
        boolean fieldFlag = false;
        String[] fieldName = Optional.ofNullable(annotation).map(LogTrace::fieldName).orElse(null);
        Set<String> fieldNameSet = Sets.newHashSet();
        if (ArrayUtils.isNotEmpty(fieldName)) {
            fieldNameSet = Sets.newHashSet(fieldName);
            fieldFlag = true;
        }
        try {
            String[] paramNames = signature.getParameterNames();
            for (int i = 0; i < paramNames.length; i++) {
                if (args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse) {
                    //HttpServletRequest、HttpServletResponse 序列化报错，暂不处理
                    continue;
                }
                if (!fieldFlag) {
                    //如果没有指定参数，则获取所有参数
                    paramNameAndValue.put(paramNames[i], args[i]);
                    continue;
                }
                //如果指定了参数，则只获取指定参数
                if (fieldNameSet.contains(paramNames[i])) {
                    paramNameAndValue.put(paramNames[i], args[i]);
                    continue;
                }
            }
        } catch (Exception e) {
            log.warn("[LogTraceAspect]wrapRequestBody Exception.", e);
        }
        return paramNameAndValue;
    }


}
