package org.gy.framework.mq.core;


import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.core.Ordered;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 功能描述：事件注解方法处理器
 *
 * @author gy
 * @version 1.0
 */
public interface EventAnnotationMethodProcessor<A extends Annotation> extends MethodInterceptor, Ordered {

    /**
     * 获取注解类
     */
    Class<A> getAnnotationClass();

    /**
     * 查找指定bean中带注解的方法，并注册事件Bean
     *
     * @param bean     bean
     * @param beanName bean名称
     * @return 注册的事件Bean
     */
    Map<String, Object> registerAnnotationMethod(Object bean, String beanName);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
