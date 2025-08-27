package org.gy.framework.mq.core.support;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.support.CommonBoostrapAction;
import org.gy.framework.mq.core.EventAnnotationMethodProcessor;
import org.springframework.core.Ordered;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class DynamicEventStrategyRegister implements CommonBoostrapAction {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 200;
    }

    @Override
    public void destroy() {
        log.info("DynamicEventStrategyRegister destroy success.");
    }

    @Override
    public void init() {
        Map<String, EventAnnotationMethodProcessor> methodProcessorMap = SpringUtil.getBeansOfType(EventAnnotationMethodProcessor.class);
        Map<String, Object> eventBeanMap = new LinkedHashMap<>();
        Map<String, Object> currentBeanMap = SpringUtil.getBeansOfType(Object.class);
        currentBeanMap.forEach((beanName, bean) -> {
            methodProcessorMap.values().stream().map(processor -> processor.registerAnnotationMethod(bean, beanName)).forEach(eventBeanMap::putAll);
        });
        log.info("DynamicEventStrategyRegister init success, registerBean size: {}", eventBeanMap.size());
    }
}
