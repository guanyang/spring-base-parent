package org.gy.framework.lock.utils;

import cn.hutool.extra.spring.SpringUtil;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

public class InvokeUtils {

    private InvokeUtils() {
    }

    @SneakyThrows
    public static Object invokeFallback(JoinPoint joinPoint, Throwable exception, String fallbackMethodName, Class<?> fallbackBeanClass) {
        Object targetBean = (fallbackBeanClass == Void.class) ? joinPoint.getTarget() : SpringUtil.getBean(fallbackBeanClass);
        // 如果fallback方法不存在，直接抛出异常
        if (!StringUtils.hasText(fallbackMethodName) || targetBean == null) {
            throw exception;
        }

        Object[] originalArgs = joinPoint.getArgs();
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?>[] parameterTypes = currentMethod.getParameterTypes();
        try {
            //先尝试获取无exception参数的 `fallback` 方法
            Method fallbackMethod = targetBean.getClass().getDeclaredMethod(fallbackMethodName, parameterTypes);
            return fallbackMethod.invoke(targetBean, originalArgs);
        } catch (NoSuchMethodException e) {
            //尝试获取带exception的方法
            Class<? extends Throwable> exceptionClass = exception.getClass();
            Method fallbackMethod = targetBean.getClass().getDeclaredMethod(fallbackMethodName, getParameterTypesWithException(parameterTypes, exceptionClass));
            Object[] fallbackArgs = new Object[originalArgs.length + 1];
            System.arraycopy(originalArgs, 0, fallbackArgs, 0, originalArgs.length);
            fallbackArgs[fallbackArgs.length - 1] = exception;
            return fallbackMethod.invoke(targetBean, fallbackArgs);
        }
    }

    public static Class<?>[] getParameterTypesWithException(Class<?>[] parameterTypes, Class<? extends Throwable> exceptionTypes) {
        Class<?>[] paramTypes = new Class<?>[parameterTypes.length + 1];
        for (int i = 0; i < parameterTypes.length; i++) {
            paramTypes[i] = parameterTypes[i];
        }
        paramTypes[parameterTypes.length] = exceptionTypes;
        return paramTypes;
    }
}
