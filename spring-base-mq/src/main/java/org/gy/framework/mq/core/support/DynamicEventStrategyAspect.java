package org.gy.framework.mq.core.support;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.mq.core.EventAnnotationMethodProcessor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.gy.framework.mq.core.support.AbstractEventAnnotationMethodProcessor.uniqueKey;

@Slf4j
public class DynamicEventStrategyAspect implements BeanFactoryPostProcessor {

    public static final String BEAN_SUFFIX = "EventAdvisor";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, EventAnnotationMethodProcessor> methodProcessorMap = beanFactory.getBeansOfType(EventAnnotationMethodProcessor.class);
        Map<String, Object> advisorBeanMap = new LinkedHashMap<>();
        methodProcessorMap.forEach((processorBeanName, methodProcessor) -> {
            Pointcut pointcut = new AnnotationMatchingPointcut(null, methodProcessor.getAnnotationClass());
            Advisor advisor = new DefaultPointcutAdvisor(pointcut, methodProcessor);
            String beanName = uniqueKey(processorBeanName, BEAN_SUFFIX);
            SpringUtil.registerBean(beanName, advisor);
            advisorBeanMap.put(beanName, advisor);
        });
        log.info("DynamicEventStrategyAspect init success, registerBean size: {}", advisorBeanMap.size());
    }
}
