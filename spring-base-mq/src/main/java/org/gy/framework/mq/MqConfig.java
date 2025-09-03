package org.gy.framework.mq;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.gy.framework.core.support.*;
import org.gy.framework.mq.annotation.EnableMQ;
import org.gy.framework.mq.config.MqManager;
import org.gy.framework.mq.config.MqManagerAction;
import org.gy.framework.mq.config.MqProperties;
import org.gy.framework.mq.config.support.DefaultMqManager;
import org.gy.framework.mq.core.EventLogService;
import org.gy.framework.mq.core.EventMessageDispatchService;
import org.gy.framework.mq.core.EventMessageHandler;
import org.gy.framework.mq.core.TraceService;
import org.gy.framework.mq.core.support.*;
import org.gy.framework.mq.model.IEventType;
import org.gy.framework.mq.model.IMessageType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Configuration
@EnableConfigurationProperties(MqProperties.class)
@EnableAutoConfiguration(exclude = {RocketMQAutoConfiguration.class, KafkaAutoConfiguration.class})
@ComponentScan(basePackageClasses = MqConfig.class)
public class MqConfig implements ImportAware, EnvironmentAware, BeanFactoryPostProcessor, CommonBoostrapAction {

    public static final Set<Class<?>> DEFAULT_ASSIGNABLE_CLASSES = Sets.newHashSet(IMessageType.class, IEventType.class);

    private AnnotationAttributes enableAsync;

    /**
     * 获取 EnableMQ 注解所在类名称
     */
    private String enableMQClassName;

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
    public DynamicEventStrategyAspect dynamicEventStrategyAspect() {
        return new DynamicEventStrategyAspect();
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
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableAsync = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableMQ.class.getName()));
        if (this.enableAsync == null) {
            throw new IllegalArgumentException("@EnableMQ is not present on importing class " + importMetadata.getClassName());
        }
        this.enableMQClassName = importMetadata.getClassName();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 400;
    }

    @Override
    public void destroy() {
        log.info("MqConfig destroy success.");
    }

    @Override
    public void init() {
        CommonServiceScanAnnotationParser parser = new CommonServiceScanAnnotationParser(this.enableAsync, this.environment, this.beanFactory);
        //默认添加EnableMQ注解所在包和MqConfig所在包扫描
        Map<String, Object> registerBean = parser.parseAndRegister(() -> Sets.newHashSet(ClassUtils.getPackageName(enableMQClassName), ClassUtils.getPackageName(MqConfig.class)));
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
