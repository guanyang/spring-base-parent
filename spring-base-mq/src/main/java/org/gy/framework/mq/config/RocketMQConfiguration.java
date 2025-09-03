package org.gy.framework.mq.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.gy.framework.mq.annotation.ConditionalOnNonEmptyCollection;
import org.gy.framework.mq.core.support.DefaultRocketMqMessageHandler;
import org.gy.framework.mq.listener.DefaultNormalListener;
import org.gy.framework.mq.listener.DefaultOrderlyListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.gy.framework.mq.config.MqProperties.ROCKET_PREFIX;

@Configuration
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnNonEmptyCollection(prefix = ROCKET_PREFIX)
public class RocketMQConfiguration {

    @Bean
    @ConditionalOnMissingBean(RocketMqManager.class)
    public RocketMqManager rocketMqManager(MqProperties properties) {
        return new RocketMqManager(properties);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultRocketMqMessageHandler.class)
    public DefaultRocketMqMessageHandler defaultRocketMqMessageHandler(RocketMqManager rocketMqManager) {
        return new DefaultRocketMqMessageHandler(rocketMqManager);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultNormalListener.class)
    public DefaultNormalListener defaultNormalListener() {
        return new DefaultNormalListener();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultOrderlyListener.class)
    public DefaultOrderlyListener defaultOrderlyListener() {
        return new DefaultOrderlyListener();
    }
}
