package io.github.guanyang.mq;

import io.github.guanyang.core.support.CommonBoostrapManager;
import io.github.guanyang.core.support.CommonServiceManager;
import io.github.guanyang.mq.config.*;
import io.github.guanyang.mq.config.support.DefaultMqManager;
import io.github.guanyang.mq.core.EventLogService;
import io.github.guanyang.mq.core.EventMessageDispatchService;
import io.github.guanyang.mq.core.EventMessageHandler;
import io.github.guanyang.mq.core.TraceService;
import io.github.guanyang.mq.core.support.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author guanyang
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(MqProperties.class)
@Import({KafkaConfiguration.class, RocketMQConfiguration.class})
public class MQAutoConfiguration {
    //需要排除的全限定类名，类存在则排除，不存在则忽略
    public static final String ROCKETMQ = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration";
    public static final String KAFKA = "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration";

    @Bean
    @ConditionalOnMissingBean(EventLogService.class)
    public EventLogService eventLogService() {
        return new DefaultEventLogServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(TraceService.class)
    public TraceService traceService() {
        return new DefaultTraceServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(EventMessageDispatchService.class)
    public EventMessageDispatchService eventMessageDispatchService() {
        return new DefaultEventMessageDispatchServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultEventAnnotationMethodProcessor.class)
    public DefaultEventAnnotationMethodProcessor defaultEventAnnotationMethodProcessor(ObjectProvider<EventLogService> eventLogService) {
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
    public MqManager mqManager(List<MqManagerAction<?, ?>> actions, List<EventMessageHandler> messageHandlers) {
        return new DefaultMqManager(actions, messageHandlers);
    }

    @AutoConfiguration
    @EnableAutoConfiguration(excludeName = {ROCKETMQ, KAFKA})
    public static class ExcludeAutoConfiguration {

    }
}
