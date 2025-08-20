package org.gy.framework.mq.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.gy.framework.core.support.CommonBoostrapAction;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.model.IMessageType;
import org.springframework.util.Assert;

import java.util.*;
import java.util.Map.Entry;

@Slf4j
public class RocketMqManager implements CommonBoostrapAction {

    private final Map<String, RocketMqProducer> producerMap = Maps.newConcurrentMap();
    private final Map<String, RocketMqConsumer> consumerMap = Maps.newConcurrentMap();
    private final Map<MessageListener, Set<String>> listenerFilterMap = Maps.newConcurrentMap();

    private final RocketMQPropertiesMap propertiesMap;

    public RocketMqManager(RocketMQPropertiesMap inventoryPropertiesMap) {
        Assert.notNull(inventoryPropertiesMap, () -> "RocketMQPropertiesMap must not be null");
        this.propertiesMap = inventoryPropertiesMap;
    }

    public RocketMqProducer getProducer(String messageTypeCode) {
        RocketMqProducer producer = Optional.ofNullable(messageTypeCode).map(producerMap::get).orElse(null);
        Assert.notNull(producer, () -> "RocketMqProducer must not be null: " + messageTypeCode);
        return producer;
    }

    public Set<String> getSupportMessageType(MessageListener messageListener) {
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
        RocketMQPropertiesMap rocketMQPropertiesMap = Optional.ofNullable(propertiesMap).orElseGet(RocketMQPropertiesMap::new);
        for (Entry<String, RocketMQProperties> entry : rocketMQPropertiesMap.entrySet()) {
            register(entry.getKey(), entry.getValue());
        }
        log.info("RocketMQManager init success, properties keys: {}", rocketMQPropertiesMap.keySet());
    }

    private void register(String code, RocketMQProperties properties) throws Exception {
        Assert.notNull(properties, () -> "RocketMQProperties properties must not be null");
        Assert.hasText(code, () -> "IMessageType code must not be null");

        IMessageType messageType = CommonServiceManager.getServiceOptional(IMessageType.class, code).orElse(null);
        Assert.notNull(messageType, () -> "IMessageType code not registered: " + code);
        if (properties.getProducer() != null) {
            RocketMqProducer rocketMqProducer = producerMap.computeIfAbsent(messageType.getCode(), k -> new RocketMqProducer(properties));
            rocketMqProducer.afterPropertiesSet();
        }
        if (properties.getConsumer() != null) {
            RocketMqConsumer rocketMqConsumer = consumerMap.computeIfAbsent(messageType.getCode(), k -> new RocketMqConsumer(properties));
            rocketMqConsumer.afterPropertiesSet();
            MessageListener messageListener = rocketMqConsumer.getMessageListener();
            listenerFilterMap.computeIfAbsent(messageListener, k -> Sets.newHashSet()).add(messageType.getCode());
        }
    }


    public static class RocketMQPropertiesMap extends HashMap<String, RocketMQProperties> {
        private static final long serialVersionUID = 414328690643875826L;
    }
}
