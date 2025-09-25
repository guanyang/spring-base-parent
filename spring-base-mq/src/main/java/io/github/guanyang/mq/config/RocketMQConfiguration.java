package io.github.guanyang.mq.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import io.github.guanyang.mq.annotation.ConditionalOnNonEmptyCollection;
import io.github.guanyang.mq.core.support.DefaultRocketMqMessageHandler;
import io.github.guanyang.mq.listener.DefaultNormalListener;
import io.github.guanyang.mq.listener.DefaultOrderlyListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.guanyang.mq.config.MqProperties.ROCKET_PREFIX;

@Configuration(proxyBeanMethods = false)
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
