package org.gy.framework.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.gy.framework.mq.annotation.ConditionalOnNonEmptyCollection;
import org.gy.framework.mq.config.support.KafkaDefaultErrorHandler;
import org.gy.framework.mq.core.EventLogService;
import org.gy.framework.mq.core.support.DefaultKafkaMessageHandler;
import org.gy.framework.mq.listener.DefaultKafkaListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import static org.gy.framework.mq.config.MqProperties.KAFKA_PREFIX;

@Slf4j
@Configuration(proxyBeanMethods = false)
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
    public DefaultErrorHandler defaultErrorHandler(ObjectProvider<MqProperties> properties, ObjectProvider<EventLogService> eventLogService) {
        DefaultErrorHandler errorHandler = new KafkaDefaultErrorHandler(properties, eventLogService);
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

    @Bean
    @ConditionalOnMissingBean(DefaultKafkaListener.class)
    public DefaultKafkaListener defaultKafkaListener() {
        return new DefaultKafkaListener();
    }
}
