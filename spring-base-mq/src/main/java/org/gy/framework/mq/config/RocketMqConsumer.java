package org.gy.framework.mq.config;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.gy.framework.mq.config.support.TraceConsumeMessageHook;
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
public class RocketMqConsumer implements InitializingBean, DisposableBean {

    private final AtomicBoolean init = new AtomicBoolean(false);

    private final RocketMQProperties rocketMQProperties;

    private final String groupName;

    private final MessageListener messageListener;

    private DefaultMQPushConsumer consumer;

    public RocketMqConsumer(final RocketMQProperties rocketMQProperties) {
        this.rocketMQProperties = checkConfig(rocketMQProperties);
        this.groupName = rocketMQProperties.getConsumer().getGroupName();
        String listenerBeanName = rocketMQProperties.getConsumer().getListenerBeanName();
        Assert.hasText(listenerBeanName, () -> "Consumer listenerBeanName must not be null: " + groupName);
        MessageListener listener = SpringUtil.getBean(listenerBeanName, MessageListener.class);
        Assert.notNull(listener, () -> "MessageListener not found: " + listenerBeanName);
        this.messageListener = listener;
    }

    public RocketMqConsumer(final RocketMQProperties rocketMQProperties, final MessageListener messageListener) {
        this.rocketMQProperties = checkConfig(rocketMQProperties);
        this.groupName = rocketMQProperties.getConsumer().getGroupName();
        Assert.notNull(messageListener, () -> "MessageListener must not be null: " + groupName);
        this.messageListener = messageListener;
    }

    protected RocketMQProperties checkConfig(final RocketMQProperties properties) {
        String nameServer = properties.getNameServer();
        Assert.hasText(nameServer, () -> "RocketMQProperties.nameServer must not be null");

        String topicName = properties.getTopic();
        Assert.hasText(topicName, () -> "RocketMQProperties.topic must not be null");

        RocketMQProperties.Consumer consumerConfig = properties.getConsumer();
        Assert.notNull(consumerConfig, () -> "RocketMQProperties.consumer must not be null");

        String groupName = consumerConfig.getGroupName();
        Assert.hasText(groupName, () -> "RocketMQProperties.consumer.groupName must not be null");

        return properties;
    }

    protected DefaultMQPushConsumer initConsumer(RocketMQProperties properties, MessageListener messageListener) throws Exception {
        RocketMQProperties.Consumer consumerConfig = properties.getConsumer();

        DefaultMQPushConsumer consumer = buildConsumer(consumerConfig, messageListener);
        consumerCustomize(consumer, properties);

        return consumer;
    }

    protected DefaultMQPushConsumer buildConsumer(RocketMQProperties.Consumer consumerConfig, MessageListener messageListener) {
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer(consumerConfig.getGroupName());
        pushConsumer.registerMessageListener(messageListener);
        return pushConsumer;
    }

    protected void consumerCustomize(DefaultMQPushConsumer consumer, RocketMQProperties properties) throws Exception {
        RocketMQProperties.Consumer consumerConfig = properties.getConsumer();
        consumer.setNamesrvAddr(properties.getNameServer());
        //同一个group启动多个消费者，定义不同的名称，避免冲突
        consumer.setInstanceName(consumerConfig.getInstanceName());
        consumer.setNamespace(consumerConfig.getNamespace());
        MessageModel messageModel = MessageModel.valueOf(consumerConfig.getMessageModel());
        //消费方式，CLUSTERING/BROADCASTING
        consumer.setMessageModel(messageModel);
        consumer.subscribe(properties.getTopic(), consumerConfig.getSelectorExpression());
        consumer.setConsumeMessageBatchMaxSize(consumerConfig.getConsumeMessageBatchMaxSize());
        //最小处理线程数
        consumer.setConsumeThreadMin(consumerConfig.getConsumeThreadMin());
        //最大处理线程数
        consumer.setConsumeThreadMax(consumerConfig.getConsumeThreadMax());

        ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.valueOf(consumerConfig.getConsumeFromWhere());
        consumer.setConsumeFromWhere(consumeFromWhere);
        consumer.setAwaitTerminationMillisWhenShutdown(15000);
        consumer.registerConsumeMessageHook(new TraceConsumeMessageHook());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (init.compareAndSet(false, true)) {
            log.info("消费者启动开始: {}", groupName);
            consumer = initConsumer(rocketMQProperties, messageListener);
            consumer.start();
            log.info("消费者启动成功: {}", groupName);
        }
    }


    @Override
    public void destroy() throws Exception {
        if (init.compareAndSet(true, false)) {
            if (consumer != null) {
                log.info("消费者服务关闭开始: {}", groupName);
                consumer.shutdown();
                log.info("消费者服务关闭成功: {}", groupName);
            }
        }
    }
}
