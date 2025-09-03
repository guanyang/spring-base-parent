package org.gy.framework.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.gy.framework.mq.annotation.ConditionalOnNonEmptyCollection;
import org.gy.framework.mq.config.MqProperties.GlobalConfig;
import org.gy.framework.mq.core.EventLogService;
import org.gy.framework.mq.core.support.DefaultKafkaMessageHandler;
import org.gy.framework.mq.core.support.EventMessageHandlerParseFactory;
import org.gy.framework.mq.model.EventLogContext;
import org.gy.framework.mq.model.EventMessageHandlerContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.util.Collections;

import static org.gy.framework.mq.config.MqProperties.KAFKA_PREFIX;
import static org.gy.framework.mq.model.MqType.KAFKA;

@Slf4j
@Configuration
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnNonEmptyCollection(prefix = KAFKA_PREFIX)
public class KafkaConfiguration {

    @Bean
    @ConditionalOnMissingBean(KafkaListenerEndpointRegistry.class)
    public KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry() {
        return new KafkaListenerEndpointRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultMessageHandlerMethodFactory.class)
    public DefaultMessageHandlerMethodFactory defaultMessageHandlerMethodFactory() {
        return new DefaultMessageHandlerMethodFactory();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultErrorHandler.class)
    public DefaultErrorHandler customErrorHandler(MqProperties properties, EventLogService eventLogService) {
        GlobalConfig globalConfig = properties.getGlobalConfig();
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(globalConfig.getRetryTimes());
        backOff.setInitialInterval(globalConfig.getInitialInterval());
        backOff.setMultiplier(globalConfig.getMultiplier());
        backOff.setMaxInterval(globalConfig.getMaxInterval());
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, ex) -> {
            EventMessageHandlerContext context = EventMessageHandlerParseFactory.parse(KAFKA, record);
            if (!EventMessageHandlerContext.validate(context)) {
                log.warn("EventMessageHandlerContext validate fail: {}", record, ex);
                return;
            }
            EventLogContext.handleEventLog(Collections.singletonList(context.getEventMessage()), ex, eventLogService::batchSaveEventLog);
        }, backOff);
        // 配置哪些异常不触发重试
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }

    @Bean
    @ConditionalOnMissingBean(KafkaManager.class)
    public KafkaManager kafkaManager(MqProperties properties, KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
        return new KafkaManager(properties, kafkaListenerEndpointRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultKafkaMessageHandler.class)
    public DefaultKafkaMessageHandler defaultKafkaMessageHandler(KafkaManager kafkaManager) {
        return new DefaultKafkaMessageHandler(kafkaManager);
    }
}
