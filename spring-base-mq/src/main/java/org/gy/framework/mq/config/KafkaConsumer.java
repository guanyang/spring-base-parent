package org.gy.framework.mq.config;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.gy.framework.mq.config.MqProperties.ConsumerListener;
import org.gy.framework.mq.config.MqProperties.KafkaProperty;
import org.gy.framework.mq.config.support.KafkaConsumerContextInterceptor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Consumer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Listener;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Getter
public class KafkaConsumer implements InitializingBean, DisposableBean {

    private static final String DEFAULT_METHOD_NAME = "onMessage";
    private static final String GENERATED_ID_PREFIX = "org.springframework.kafka.KafkaListenerEndpointContainer#";
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final KafkaProperty kafkaProperty;

    private final String topic;

    private final AcknowledgingMessageListener<?, ?> messageListener;

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private MessageListenerContainer consumer;

    public KafkaConsumer(final KafkaProperty kafkaProperty, KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
        this.kafkaProperty = checkConfig(kafkaProperty);
        this.topic = kafkaProperty.getTopic();
        String listenerBeanName = kafkaProperty.getListener().getListenerBeanName();
        AcknowledgingMessageListener<?, ?> listener = SpringUtil.getBean(listenerBeanName, AcknowledgingMessageListener.class);
        Assert.notNull(listener, () -> "AcknowledgingMessageListener not found: " + listenerBeanName);
        this.messageListener = listener;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }

    protected KafkaProperty checkConfig(final KafkaProperty properties) {
        List<String> bootstrapServers = properties.getBootstrapServers();
        Assert.notEmpty(bootstrapServers, () -> "KafkaProperty.bootstrapServers must not be null");

        String topicName = properties.getTopic();
        Assert.hasText(topicName, () -> "KafkaProperty.topic must not be null");

        Consumer consumer = properties.getConsumer();
        Assert.notNull(consumer, () -> "KafkaProperty.consumer must not be null");

        String groupId = consumer.getGroupId();
        Assert.hasText(groupId, () -> "KafkaProperty.consumer.groupId must not be null");

        ConsumerListener listener = properties.getListener();
        Assert.notNull(listener, () -> "KafkaProperty.listener must not be null");

        String listenerBeanName = listener.getListenerBeanName();
        Assert.hasText(listenerBeanName, () -> "KafkaProperty.listener listenerBeanName must not be null: " + groupId);

        return properties;
    }

    protected MessageListenerContainer initConsumer() {
        ConcurrentKafkaListenerContainerFactory<?, ?> containerFactory = kafkaListenerContainerFactory(kafkaProperty);
        MethodKafkaListenerEndpoint<?, ?> endpoint = kafkaListenerEndpoint(kafkaProperty, messageListener);
        kafkaListenerEndpointRegistry.registerListenerContainer(endpoint, containerFactory, false);
        return kafkaListenerEndpointRegistry.getListenerContainer(endpoint.getId());
    }

    protected ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(KafkaProperty kafkaProperty) {
        ConsumerFactory<Object, Object> kafkaConsumerFactory = kafkaConsumerFactory(kafkaProperty);
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory);
        DefaultErrorHandler errorHandler = SpringUtil.getBean(DefaultErrorHandler.class);
        Assert.notNull(errorHandler, () -> "DefaultErrorHandler not found");
        factory.setCommonErrorHandler(errorHandler);
        factory.setRecordInterceptor(new KafkaConsumerContextInterceptor());
        configureListenerFactory(kafkaProperty, factory);
        configureContainer(kafkaProperty, factory.getContainerProperties());
        return factory;
    }


    protected ConsumerFactory<Object, Object> kafkaConsumerFactory(KafkaProperty kafkaProperty) {
        DefaultKafkaConsumerFactory<Object, Object> factory = new DefaultKafkaConsumerFactory<>(kafkaProperty.buildConsumerProperties());
        Map<String, DefaultKafkaConsumerFactoryCustomizer> customizerMap = SpringUtil.getBeansOfType(DefaultKafkaConsumerFactoryCustomizer.class);
        customizerMap.values().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    protected MethodKafkaListenerEndpoint<?, ?> kafkaListenerEndpoint(KafkaProperty kafkaProperty, MessageListener<?, ?> messageListener) {
        Method method = ReflectionUtils.findMethod(messageListener.getClass(), DEFAULT_METHOD_NAME, ConsumerRecord.class, Acknowledgment.class);
        Assert.notNull(method, () -> "AcknowledgingMessageListener Method not found: " + DEFAULT_METHOD_NAME);
        MethodKafkaListenerEndpoint<?, ?> endpoint = new MethodKafkaListenerEndpoint<>();
        endpoint.setMethod(method);
        endpoint.setBean(messageListener);
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory = SpringUtil.getBean(DefaultMessageHandlerMethodFactory.class);
        endpoint.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
        endpoint.setId(getEndpointId());
        String groupId = Optional.ofNullable(kafkaProperty.getConsumer()).map(Consumer::getGroupId).orElseGet(endpoint::getId);
        endpoint.setGroupId(groupId);
        endpoint.setTopics(topic);
        return endpoint;
    }


    @Override
    public void destroy() throws Exception {
        if (consumer != null) {
            log.info("kafka消费者服务关闭开始: {}", topic);
            consumer.destroy();
            log.info("kafka消费者服务关闭成功: {}", topic);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("kafka消费者启动开始: {}", topic);
        consumer = initConsumer();
        consumer.start();
        log.info("kafka消费者启动成功: {}", topic);
    }

    protected void configureListenerFactory(KafkaProperty kafkaProperty, ConcurrentKafkaListenerContainerFactory<Object, Object> factory) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        Listener properties = kafkaProperty.getListener();
        map.from(properties::getConcurrency).to(factory::setConcurrency);
    }

    protected void configureContainer(KafkaProperty kafkaProperty, ContainerProperties container) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        Listener properties = kafkaProperty.getListener();
        map.from(properties::getAckMode).to(container::setAckMode);
        map.from(properties::getClientId).to(container::setClientId);
        map.from(properties::getAckCount).to(container::setAckCount);
        map.from(properties::getAckTime).as(Duration::toMillis).to(container::setAckTime);
        map.from(properties::getPollTimeout).as(Duration::toMillis).to(container::setPollTimeout);
        map.from(properties::getNoPollThreshold).to(container::setNoPollThreshold);
        map.from(properties.getIdleBetweenPolls()).as(Duration::toMillis).to(container::setIdleBetweenPolls);
        map.from(properties::getIdleEventInterval).as(Duration::toMillis).to(container::setIdleEventInterval);
        map.from(properties::getMonitorInterval).as(Duration::getSeconds).as(Number::intValue).to(container::setMonitorInterval);
        map.from(properties::getLogContainerConfig).to(container::setLogContainerConfig);
        map.from(properties::isOnlyLogRecordMetadata).to(container::setOnlyLogRecordMetadata);
        map.from(properties::isMissingTopicsFatal).to(container::setMissingTopicsFatal);
    }

    protected String getEndpointId() {
        return GENERATED_ID_PREFIX + COUNTER.getAndIncrement();
    }
}
