package io.github.guanyang.mq.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import io.github.guanyang.mq.config.support.DefaultMQProducerWrapper;
import io.github.guanyang.mq.config.support.RocketMQProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Getter
public class RocketMqProducer implements InitializingBean, DisposableBean {

    private final AtomicBoolean init = new AtomicBoolean(false);

    private final RocketMQProperties rocketMQProperties;

    private final String groupName;

    private DefaultMQProducer producer;

    public RocketMqProducer(final RocketMQProperties rocketMQProperties) {
        this.rocketMQProperties = checkConfig(rocketMQProperties);
        this.groupName = rocketMQProperties.getProducer().getGroupName();
    }

    protected RocketMQProperties checkConfig(final RocketMQProperties properties) {
        String nameServer = properties.getNameServer();
        Assert.hasText(nameServer, () -> "RocketMQProperties.nameServer must not be null");

        String topicName = properties.getTopic();
        Assert.hasText(topicName, () -> "RocketMQProperties.topic must not be null");

        RocketMQProperties.Producer producerConfig = properties.getProducer();
        Assert.notNull(producerConfig, () -> "RocketMQProperties.producer must not be null");

        String groupName = producerConfig.getGroupName();
        Assert.hasText(groupName, () -> "RocketMQProperties.producer.groupName must not be null");

        return properties;
    }

    protected DefaultMQProducer initProducer(RocketMQProperties properties) {
        RocketMQProperties.Producer producerConfig = properties.getProducer();

        DefaultMQProducer producerWrapper = buildProducer(producerConfig);
        producerCustomize(producerWrapper, properties);

        return producerWrapper;
    }

    protected DefaultMQProducer buildProducer(RocketMQProperties.Producer producerConfig) {
        return new DefaultMQProducerWrapper(producerConfig.getGroupName());
    }

    protected void producerCustomize(DefaultMQProducer producerWrapper, RocketMQProperties properties) {
        RocketMQProperties.Producer producerConfig = properties.getProducer();
        producerWrapper.setNamesrvAddr(properties.getNameServer());
        //同一个group定义多个实例，需要定义不同的实例名称，避免冲突
        producerWrapper.setInstanceName(producerConfig.getInstanceName());
        producerWrapper.setNamespace(producerConfig.getNamespace());
        producerWrapper.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
        producerWrapper.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
        producerWrapper.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
        producerWrapper.setMaxMessageSize(producerConfig.getMaxMessageSize());
        producerWrapper.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMessageBodyThreshold());
        producerWrapper.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryNextServer());
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        if (init.compareAndSet(false, true)) {
            log.info("RocketMQ生产者启动开始: {}", groupName);
            producer = initProducer(rocketMQProperties);
            producer.start();
            log.info("RocketMQ生产者启动成功: {}", groupName);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (init.compareAndSet(true, false)) {
            if (producer != null) {
                log.info("RocketMQ生产者服务关闭开始: {}", groupName);
                producer.shutdown();
                log.info("RocketMQ生产者服务关闭成功: {}", groupName);
            }
        }
    }

}
