package org.gy.framework.mq;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.support.CommonBoostrapManager;
import org.gy.framework.core.support.CommonServiceAction;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.core.support.CommonServiceScanAnnotationParser;
import org.gy.framework.mq.annotation.EnableMQ;
import org.gy.framework.mq.config.*;
import org.gy.framework.mq.config.support.DefaultMqManager;
import org.gy.framework.mq.core.*;
import org.gy.framework.mq.core.support.*;
import org.gy.framework.mq.model.IEventType;
import org.gy.framework.mq.model.IMessageType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.gy.framework.mq.MQAutoConfiguration.KAFKA;
import static org.gy.framework.mq.MQAutoConfiguration.ROCKETMQ;

@Slf4j
@Configuration
@EnableConfigurationProperties(MqProperties.class)
@EnableAutoConfiguration(excludeName = {ROCKETMQ, KAFKA})
@Import({KafkaConfiguration.class, RocketMQConfiguration.class})
public class MQAutoConfiguration implements ImportAware, EnvironmentAware, BeanFactoryAware, InitializingBean, DisposableBean {
    //需要排除的全限定类名，类存在则排除，不存在则忽略
    public static final String ROCKETMQ = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration";
    public static final String KAFKA = "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration";

    public static final Set<Class<?>> DEFAULT_ASSIGNABLE_CLASSES = Sets.newHashSet(IMessageType.class, IEventType.class);

    private AnnotationAttributes importAttributes;
    private AnnotationMetadata importMetadata;

    private Environment environment;

    private ConfigurableListableBeanFactory beanFactory;

    @Bean
    @ConditionalOnMissingBean(EventLogService.class)
    public EventLogService defaultEventLogService() {
        return new DefaultEventLogServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(TraceService.class)
    public TraceService defaultTraceService() {
        return new DefaultTraceServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(EventMessageDispatchService.class)
    public EventMessageDispatchService defaultEventMessageDispatchService() {
        return new DefaultEventMessageDispatchServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultEventAnnotationMethodProcessor.class)
    public DefaultEventAnnotationMethodProcessor defaultEventAnnotationMethodProcessor(EventLogService eventLogService) {
        return new DefaultEventAnnotationMethodProcessor(eventLogService);
    }

    @Bean
    @ConditionalOnMissingBean(DynamicEventStrategyAspect.class)
    public DynamicEventStrategyAspect dynamicEventStrategyAspect(Map<String, EventAnnotationMethodProcessor<?>> methodProcessorMap) {
        return new DynamicEventStrategyAspect(methodProcessorMap);
    }

    @Bean
    @ConditionalOnMissingBean(EventMessageProducerRegister.class)
    public EventMessageProducerRegister eventMessageProducerRegister(MqProperties properties) {
        return new EventMessageProducerRegister(properties);
    }

    @Bean
    @ConditionalOnMissingBean(DynamicEventStrategyRegister.class)
    public DynamicEventStrategyRegister dynamicEventStrategyRegister() {
        return new DynamicEventStrategyRegister();
    }

    @Bean
    @ConditionalOnMissingBean(CommonServiceManager.class)
    public CommonServiceManager commonServiceManager() {
        return new CommonServiceManager();
    }

    @Bean
    @ConditionalOnMissingBean(CommonBoostrapManager.class)
    public CommonBoostrapManager commonBoostrapManager() {
        return new CommonBoostrapManager();
    }

    @Bean
    @ConditionalOnMissingBean(MqManager.class)
    public MqManager defaultMqManager(List<MqManagerAction<?, ?>> actions, List<EventMessageHandler> messageHandlers) {
        return new DefaultMqManager(actions, messageHandlers);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.importMetadata = importMetadata;
        this.importAttributes = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableMQ.class.getName()));
        Assert.notNull(importAttributes, () -> "@EnableMQ is not present on importing class: " + importMetadata.getClassName());
    }

    @Override
    public void destroy() {
        log.info("MqConfig destroy success.");
    }

    @Override
    public void afterPropertiesSet() {
        CommonServiceScanAnnotationParser parser = new CommonServiceScanAnnotationParser(this.importAttributes, this.environment, this.beanFactory);
        //默认添加EnableMQ注解所在包和MqConfig所在包扫描
        Map<String, Object> registerBean = parser.parseAndRegister(importMetadata, () -> Sets.newHashSet(ClassUtils.getPackageName(MQAutoConfiguration.class)));
        registerBean.forEach((beanName, bean) -> beanInit(bean));
        log.info("MqConfig init success, registerBean size: {}", registerBean.size());
    }

    private void beanInit(Object bean) {
        //指定bean需要提前初始化，因为注册动态事件时需要使用
        boolean match = DEFAULT_ASSIGNABLE_CLASSES.stream().anyMatch(clazz -> clazz.isInstance(bean));
        if (match && bean instanceof CommonServiceAction) {
            ((CommonServiceAction) bean).init();
        }
    }
}
