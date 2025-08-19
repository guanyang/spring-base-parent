package org.gy.framework.mq.core.support;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.core.support.CommonBoostrapAction;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.annotation.DynamicEventStrategy;
import org.gy.framework.mq.core.EventMessageConsumerService;
import org.gy.framework.mq.model.DynamicEventContext;
import org.gy.framework.mq.model.IEventType;
import org.gy.framework.mq.model.IMessageType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodIntrospector.MetadataLookup;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public class DynamicEventStrategyRegister implements BeanFactoryPostProcessor, CommonBoostrapAction {

    private ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
    }

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
        String[] beanDefinitionNames = configurableListableBeanFactory.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = configurableListableBeanFactory.getBean(beanDefinitionName);
            processAfterInitialization(bean, beanDefinitionName);
        }
        log.info("DynamicEventStrategyRegister init success.");
    }

    protected void processAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        Map<Method, DynamicEventStrategy> methodMap = findMethod(beanName, beanClass);
        if (MapUtils.isEmpty(methodMap)) {
            return;
        }
        for (Map.Entry<Method, DynamicEventStrategy> entry : methodMap.entrySet()) {
            Method method = entry.getKey();
            DynamicEventStrategy annotation = entry.getValue();
            DynamicEventContext<Object, Object> ctx = build(bean, method, annotation);
            // 注册 Bean
            EventMessageConsumerService eventService = new DefaultDynamicEventMessageServiceImpl(ctx);
            String eventBeanName = uniqueKey(beanName, method.getName());
            configurableListableBeanFactory.registerSingleton(eventBeanName, eventService);
        }
    }


    private static DynamicEventContext<Object, Object> build(Object bean, Method method, DynamicEventStrategy annotation) {
        Class<?> clazz = bean.getClass();
        String methodName = method.getName();
        Type[] paramTypes = method.getGenericParameterTypes();
        if (paramTypes.length != 1) {
            throw new IllegalStateException("DynamicEventStrategy method invalid, only one param support, for[" + clazz + "#" + methodName + "].");
        }
        method.setAccessible(true);
        Function<Object, Object> executeFunction = data -> ReflectionUtils.invokeMethod(method, bean, data);
        Predicate<Throwable> supportRetry = DynamicEventContext.getRetryPredicate(annotation.supportRetry());

        Type dataType = paramTypes[0];
        String eventTypeCode = annotation.eventTypeCode();
        IEventType eventType = CommonServiceManager.getServiceOptional(IEventType.class, eventTypeCode).orElse(null);
        Assert.notNull(eventType, () -> "IEventType code not support: " + eventTypeCode);

        String messageTypeCode = annotation.messageTypeCode();
        IMessageType messageType = CommonServiceManager.getServiceOptional(IMessageType.class, messageTypeCode).orElse(null);
        Assert.notNull(messageType, () -> "IMessageType code not support: " + messageTypeCode);

        return new DynamicEventContext<>(eventType, dataType, executeFunction, supportRetry, messageType);
    }


    private static Map<Method, DynamicEventStrategy> findMethod(String beanDefinitionName, Class<?> clazz) {
        Map<Method, DynamicEventStrategy> annotatedMethods = null;
        try {
            annotatedMethods = MethodIntrospector.selectMethods(clazz, (MetadataLookup<DynamicEventStrategy>) method -> AnnotatedElementUtils.findMergedAnnotation(method, DynamicEventStrategy.class));
        } catch (Throwable ex) {
            log.warn("DynamicEventStrategyRegister findMethod error for bean[{}].", beanDefinitionName, ex);
        }
        return annotatedMethods;
    }

    private static String uniqueKey(Object... keys) {
        return StringUtils.joinWith(StrUtil.UNDERLINE, keys);
    }
}
