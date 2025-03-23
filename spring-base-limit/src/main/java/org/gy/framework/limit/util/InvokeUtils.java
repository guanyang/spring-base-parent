package org.gy.framework.limit.util;

import cn.hutool.extra.spring.SpringUtil;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author guanyang
 */
public class InvokeUtils {

    private InvokeUtils() {
    }

    /**
     * 执行自定义函数
     */
    @SneakyThrows
    public static Object invokeFunction(String functionName, JoinPoint joinPoint) {
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object targetBean = joinPoint.getTarget();
        Method handleMethod;
        try {
            handleMethod = targetBean.getClass().getMethod(functionName, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invoke functionName not support: " + functionName, e);
        }

        Object[] args = joinPoint.getArgs();
        Object res;
        try {
            res = handleMethod.invoke(targetBean, args);
        } catch (Exception e) {
            throw new IllegalStateException("Fail to invoke functionName: " + functionName, e);
        }
        return res;
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
            Method fallbackMethod = targetBean.getClass().getMethod(fallbackMethodName, parameterTypes);
            fallbackMethod.setAccessible(true);
            return fallbackMethod.invoke(targetBean, originalArgs);
        } catch (NoSuchMethodException e) {
            //尝试获取带exception的方法
            Class<? extends Throwable> exceptionClass = exception.getClass();
            Method fallbackMethod = targetBean.getClass().getDeclaredMethod(fallbackMethodName, getParameterTypesWithException(parameterTypes, exceptionClass));
            fallbackMethod.setAccessible(true);
            Object[] fallbackArgs = new Object[originalArgs.length + 1];
            System.arraycopy(originalArgs, 0, fallbackArgs, 0, originalArgs.length);
            fallbackArgs[originalArgs.length] = exception;
            return fallbackMethod.invoke(targetBean, fallbackArgs);
        }
    }

    public static Class<?>[] getParameterTypesWithException(Class<?>[] parameterTypes, Class<? extends Throwable> exceptionTypes) {
        Class<?>[] paramTypes = new Class<?>[parameterTypes.length + 1];
        System.arraycopy(parameterTypes, 0, paramTypes, 0, parameterTypes.length);
        paramTypes[parameterTypes.length] = exceptionTypes;
        return paramTypes;
    }
}
