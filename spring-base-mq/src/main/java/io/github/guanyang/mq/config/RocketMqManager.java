package io.github.guanyang.mq.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import io.github.guanyang.core.support.CommonServiceManager;
import io.github.guanyang.mq.config.support.RocketMQProperties;
import io.github.guanyang.mq.model.IMessageType;
import io.github.guanyang.mq.model.MqType;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class RocketMqManager implements MqManagerAction<RocketMqProducer, RocketMqConsumer> {

    private final Map<String, RocketMqProducer> producerMap = Maps.newConcurrentMap();
    private final Map<String, RocketMqConsumer> consumerMap = Maps.newConcurrentMap();
    private final Map<MessageListener, Set<String>> listenerFilterMap = Maps.newConcurrentMap();

    private final MqProperties properties;

    public RocketMqManager(MqProperties properties) {
        this.properties = properties;
    }

    @Override
    public RocketMqProducer getProducer(String messageTypeCode) {
        Assert.hasText(messageTypeCode, () -> "RocketMqProducer messageTypeCode is null");
        RocketMqProducer producer = Optional.ofNullable(messageTypeCode).map(producerMap::get).orElse(null);
        Assert.notNull(producer, () -> "RocketMqProducer not registered: " + messageTypeCode);
        return producer;
    }

    @Override
    public RocketMqConsumer getConsumer(String messageTypeCode) {
        Assert.hasText(messageTypeCode, () -> "RocketMqConsumer messageTypeCode is null");
        RocketMqConsumer consumer = Optional.ofNullable(messageTypeCode).map(consumerMap::get).orElse(null);
        Assert.notNull(consumer, () -> "RocketMqConsumer not registered: " + messageTypeCode);
        return consumer;
    }

    @Override
    public Set<String> getSupportMessageType(Object messageListener) {
        return Optional.ofNullable(listenerFilterMap.get(messageListener)).orElseGet(Collections::emptySet);
    }

    @Override
    @SneakyThrows
    public void destroy() {
        for (Entry<String, RocketMqProducer> entry : producerMap.entrySet()) {
            RocketMqProducer producer = entry.getValue();
            producer.destroy();
        }
        producerMap.clear();
        for (Entry<String, RocketMqConsumer> entry : consumerMap.entrySet()) {
            RocketMqConsumer consumer = entry.getValue();
            consumer.destroy();
        }
        consumerMap.clear();
        log.info("RocketMQManager destroy success.");
    }

    @Override
    @SneakyThrows
    public void init() {
        Map<String, RocketMQProperties> propertiesMap = Optional.ofNullable(properties.getRocketmq()).orElseGet(Collections::emptyMap);
        for (Entry<String, RocketMQProperties> entry : propertiesMap.entrySet()) {
            register(entry.getKey(), entry.getValue());
        }
        log.info("RocketMQManager init success, properties keys: {}", propertiesMap.keySet());
    }

    protected void register(String code, RocketMQProperties properties) throws Exception {
        Assert.notNull(properties, () -> "RocketMQProperties properties must not be null");
        Assert.hasText(code, () -> "IMessageType code must not be null");

        IMessageType messageType = CommonServiceManager.getServiceOptional(IMessageType.class, code).orElse(null);
        Assert.notNull(messageType, () -> "IMessageType code not registered: " + code);
        registerProducer(messageType, properties, producerMap);
        registerConsumer(messageType, properties, consumerMap, listenerFilterMap);
    }

    protected void registerProducer(IMessageType messageType, RocketMQProperties properties, Map<String, RocketMqProducer> producerMap) throws Exception {
        if (properties.getProducer() != null) {
            RocketMqProducer rocketMqProducer = producerMap.computeIfAbsent(messageType.getCode(), k -> new RocketMqProducer(properties));
            rocketMqProducer.afterPropertiesSet();
        }
    }

    protected void registerConsumer(IMessageType messageType, RocketMQProperties properties, Map<String, RocketMqConsumer> consumerMap, Map<MessageListener, Set<String>> listenerFilterMap) throws Exception {
        if (properties.getConsumer() != null) {
            RocketMqConsumer rocketMqConsumer = consumerMap.computeIfAbsent(messageType.getCode(), k -> new RocketMqConsumer(properties));
            rocketMqConsumer.afterPropertiesSet();
            MessageListener messageListener = rocketMqConsumer.getMessageListener();
            listenerFilterMap.computeIfAbsent(messageListener, k -> Sets.newHashSet()).add(messageType.getCode());
        }
    }

    @Override
    public MqType getMqType() {
        return MqType.ROCKETMQ;
    }
}
