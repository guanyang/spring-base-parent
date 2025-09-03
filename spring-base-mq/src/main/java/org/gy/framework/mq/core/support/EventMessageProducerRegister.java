package org.gy.framework.mq.core.support;

import cn.hutool.extra.spring.SpringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.support.CommonBoostrapAction;
import org.gy.framework.mq.config.MqProperties;
import org.gy.framework.mq.config.MqProperties.KafkaProperty;
import org.gy.framework.mq.config.support.RocketMQProperties;
import org.gy.framework.mq.core.EventMessageProducerService;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Slf4j
public class EventMessageProducerRegister implements CommonBoostrapAction {

    private final MqProperties properties;

    public EventMessageProducerRegister(MqProperties properties) {
        this.properties = properties;
    }

    @Override
    @SneakyThrows
    public void init() {
        if (properties == null) {
            log.info("MQProperties is null, ignore register.");
            return;
        }
        Map<String, Object> beanMap = new LinkedHashMap<>();
        //获取已经注册的消息类型，避免重复注册
        Set<String> registryMessageTypes = getRegistryMessageTypes();
        Map<String, RocketMQProperties> rocketMqConfigMap = Optional.ofNullable(properties.getRocketmq()).orElseGet(Collections::emptyMap);
        for (Entry<String, RocketMQProperties> entry : rocketMqConfigMap.entrySet()) {
            registerService(entry.getKey(), entry.getValue(), beanMap, registryMessageTypes);
        }
        Map<String, KafkaProperty> kafkaConfigMap = Optional.ofNullable(properties.getKafka()).orElseGet(Collections::emptyMap);
        for (Entry<String, KafkaProperty> entry : kafkaConfigMap.entrySet()) {
            registerService(entry.getKey(), entry.getValue(), beanMap, registryMessageTypes);
        }
        log.info("EventMessageProducerRegister init success, registerBean size: {}", beanMap.size());
    }

    @Override
    public void destroy() {
        log.info("EventMessageProducerRegister destroy success.");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 300;
    }

    private void registerService(String messageTypeCode, Object properties, Map<String, Object> beanMap, Set<String> registryMessageTypes) {
        Assert.notNull(properties, () -> "Properties must not be null");
        Assert.hasText(messageTypeCode, () -> "IMessageType code must not be null");
        if (registryMessageTypes.contains(messageTypeCode)) {
            log.info("EventMessageProducerRegister messageTypeCode already registered, ignore register: {}", messageTypeCode);
            return;
        }
        // 根据不同类型的properties判断是否需要创建producer
        boolean shouldCreateProducer = false;
        if (properties instanceof RocketMQProperties) {
            shouldCreateProducer = ((RocketMQProperties) properties).getProducer() != null;
        } else if (properties instanceof KafkaProperty) {
            shouldCreateProducer = ((KafkaProperty) properties).getProducer() != null;
        }
        //配置producer才创建producerService
        if (shouldCreateProducer) {
            EventMessageProducerService producerService = new DefaultEventMessageProducerServiceImpl(messageTypeCode);
            String beanName = producerService.getServiceName();
            SpringUtil.registerBean(beanName, producerService);
            beanMap.put(beanName, producerService);
        }
    }

    private Set<String> getRegistryMessageTypes() {
        Map<String, EventMessageProducerService> registedMap = SpringUtil.getBeansOfType(EventMessageProducerService.class);
        return registedMap.values().stream().map(EventMessageProducerService::getMessageTypeCode).collect(Collectors.toSet());
    }
}
