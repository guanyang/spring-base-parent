package org.gy.framework.mq.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.config.MqProperties.KafkaProperty;
import org.gy.framework.mq.model.IMessageType;
import org.gy.framework.mq.model.MqType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class KafkaManager implements MqManagerAction<KafkaProducer, KafkaConsumer> {

    private final Map<String, KafkaProducer> producerMap = Maps.newConcurrentMap();
    private final Map<String, KafkaConsumer> consumerMap = Maps.newConcurrentMap();
    private final Map<AcknowledgingMessageListener<?, ?>, Set<String>> listenerFilterMap = Maps.newConcurrentMap();

    private final MqProperties properties;
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    public KafkaManager(MqProperties properties, KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
        this.properties = properties;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }

    @Override
    public KafkaProducer getProducer(String messageTypeCode) {
        Assert.hasText(messageTypeCode, () -> "KafkaProducer messageTypeCode is null");
        KafkaProducer producer = Optional.ofNullable(messageTypeCode).map(producerMap::get).orElse(null);
        Assert.notNull(producer, () -> "KafkaProducer not registered: " + messageTypeCode);
        return producer;
    }

    @Override
    public KafkaConsumer getConsumer(String messageTypeCode) {
        Assert.hasText(messageTypeCode, () -> "KafkaConsumer messageTypeCode is null");
        KafkaConsumer consumer = Optional.ofNullable(messageTypeCode).map(consumerMap::get).orElse(null);
        Assert.notNull(consumer, () -> "KafkaConsumer not registered: " + messageTypeCode);
        return consumer;
    }

    @Override
    public Set<String> getSupportMessageType(Object messageListener) {
        return Optional.ofNullable(listenerFilterMap.get(messageListener)).orElseGet(Collections::emptySet);
    }

    @Override
    @SneakyThrows
    public void destroy() {
        for (Entry<String, KafkaProducer> entry : producerMap.entrySet()) {
            KafkaProducer producer = entry.getValue();
            producer.destroy();
        }
        producerMap.clear();
        for (Entry<String, KafkaConsumer> entry : consumerMap.entrySet()) {
            KafkaConsumer consumer = entry.getValue();
            consumer.destroy();
        }
        consumerMap.clear();
        log.info("KafkaManager destroy success.");
    }

    @Override
    @SneakyThrows
    public void init() {
        Map<String, KafkaProperty> propertiesMap = Optional.ofNullable(properties.getKafka()).orElseGet(Collections::emptyMap);
        for (Entry<String, KafkaProperty> entry : propertiesMap.entrySet()) {
            register(entry.getKey(), entry.getValue());
        }
        log.info("KafkaManager init success, properties keys: {}", propertiesMap.keySet());
    }

    protected void register(String code, KafkaProperty properties) throws Exception {
        Assert.notNull(properties, () -> "KafkaProperty must not be null");
        Assert.hasText(code, () -> "IMessageType code must not be null");

        IMessageType messageType = CommonServiceManager.getServiceOptional(IMessageType.class, code).orElse(null);
        Assert.notNull(messageType, () -> "IMessageType code not registered: " + code);
        registerProducer(messageType, properties, producerMap);
        registerConsumer(messageType, properties, kafkaListenerEndpointRegistry, consumerMap, listenerFilterMap);
    }

    protected void registerProducer(IMessageType messageType, KafkaProperty properties, Map<String, KafkaProducer> producerMap) throws Exception {
        if (properties.getProducer() != null) {
            KafkaProducer producer = producerMap.computeIfAbsent(messageType.getCode(), k -> new KafkaProducer(properties));
            producer.afterPropertiesSet();
        }
    }

    protected void registerConsumer(IMessageType messageType, KafkaProperty properties, KafkaListenerEndpointRegistry registry, Map<String, KafkaConsumer> consumerMap, Map<AcknowledgingMessageListener<?, ?>, Set<String>> listenerFilterMap) throws Exception {
        if (properties.getConsumer() != null) {
            KafkaConsumer consumer = consumerMap.computeIfAbsent(messageType.getCode(), k -> new KafkaConsumer(properties, registry));
            consumer.afterPropertiesSet();
            AcknowledgingMessageListener<?, ?> messageListener = consumer.getMessageListener();
            listenerFilterMap.computeIfAbsent(messageListener, k -> Sets.newHashSet()).add(messageType.getCode());
        }
    }

    @Override
    public MqType getMqType() {
        return MqType.KAFKA;
    }
}
