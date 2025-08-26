package org.gy.framework.mq.core.support;

import cn.hutool.extra.spring.SpringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.support.CommonBoostrapAction;
import org.gy.framework.mq.config.RocketMQProperties;
import org.gy.framework.mq.config.RocketMqManager.RocketMQPropertiesMap;
import org.gy.framework.mq.core.EventMessageProducerService;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class EventMessageProducerRegister implements CommonBoostrapAction {

    private final RocketMQPropertiesMap propertiesMap;

    public EventMessageProducerRegister(RocketMQPropertiesMap propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

    @Override
    @SneakyThrows
    public void init() {
        RocketMQPropertiesMap configMap = Optional.ofNullable(propertiesMap).orElseGet(RocketMQPropertiesMap::new);
        Map<String, Object> beanMap = new LinkedHashMap<>();
        //获取已经注册的消息类型，避免重复注册
        Set<String> registryMessageTypes = getRegistryMessageTypes();
        for (Entry<String, RocketMQProperties> entry : configMap.entrySet()) {
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

    private void registerService(String messageTypeCode, RocketMQProperties properties, Map<String, Object> beanMap, Set<String> registryMessageTypes) throws Exception {
        Assert.notNull(properties, () -> "RocketMQProperties properties must not be null");
        Assert.hasText(messageTypeCode, () -> "IMessageType code must not be null");

        if (registryMessageTypes.contains(messageTypeCode)) {
            log.info("EventMessageProducerRegister messageTypeCode already registered, ignore register: {}", messageTypeCode);
            return;
        }
        //配置producer才创建producerService
        if (properties.getProducer() != null) {
            EventMessageProducerService producerService = new DefaultRocketMQEventMessageProducerServiceImpl(messageTypeCode);
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
