package org.gy.framework.mq.core.support;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.mq.core.EventAnnotationMethodProcessor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.gy.framework.mq.core.support.AbstractEventAnnotationMethodProcessor.uniqueKey;

@Slf4j
public class DynamicEventStrategyAspect implements InitializingBean, DisposableBean {

    public static final String BEAN_SUFFIX = "EventAdvisor";

    private final Map<String, EventAnnotationMethodProcessor<?>> methodProcessorMap;

    public DynamicEventStrategyAspect(Map<String, EventAnnotationMethodProcessor<?>> methodProcessorMap) {
        this.methodProcessorMap = methodProcessorMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventAnnotationMethodProcessor<?>> processorMap = Optional.ofNullable(methodProcessorMap).orElseGet(Collections::emptyMap);
        Map<String, Object> advisorBeanMap = new LinkedHashMap<>();
        processorMap.forEach((processorBeanName, methodProcessor) -> {
            Pointcut pointcut = new AnnotationMatchingPointcut(null, methodProcessor.getAnnotationClass());
            Advisor advisor = new DefaultPointcutAdvisor(pointcut, methodProcessor);
            String beanName = uniqueKey(processorBeanName, BEAN_SUFFIX);
            SpringUtil.registerBean(beanName, advisor);
            advisorBeanMap.put(beanName, advisor);
        });
        log.info("DynamicEventStrategyAspect init success, registerBean size: {}", advisorBeanMap.size());
    }

    @Override
    public void destroy() throws Exception {
        Optional.ofNullable(methodProcessorMap).ifPresent(Map::clear);
        log.info("DynamicEventStrategyAspect destroy success.");
    }
}
