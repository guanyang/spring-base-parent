package org.gy.framework.mq.config;

import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.core.EventMessageHandler;
import org.gy.framework.mq.model.EventMessage;
import org.gy.framework.mq.model.IMessageType;
import org.gy.framework.mq.model.MqType;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

public interface MqManager {

    /**
     * 获取MqManagerAction
     *
     * @param mqType
     */
    <P, C> MqManagerAction<P, C> getManagerAction(MqType mqType);

    /**
     * 获取MessageHandler
     *
     * @param mqType
     */
    <P> EventMessageHandler getMessageHandler(MqType mqType);

    /**
     * 发送消息
     *
     * @param messageTypeCode
     * @param eventSendReqs
     */
    default <T> void publish(String messageTypeCode, List<EventMessage<T>> eventSendReqs) {
        Assert.hasText(messageTypeCode, () -> "MqManager messageTypeCode is null");
        IMessageType messageType = CommonServiceManager.getServiceOptional(IMessageType.class, messageTypeCode).orElse(null);
        Assert.notNull(messageType, () -> "IMessageType code not registered: " + messageTypeCode);

        EventMessageHandler messageHandler = getMessageHandler(messageType.getMqType());
        messageHandler.publish(messageTypeCode, eventSendReqs);
    }

    /**
     * 订阅消息
     *
     * @param mqType          MQ类型
     * @param originalMsg     原始消息
     * @param messageListener 监听器
     */
    default void subscribe(MqType mqType, Object originalMsg, Object messageListener) {
        EventMessageHandler messageHandler = getMessageHandler(mqType);
        messageHandler.subscribe(originalMsg, messageListener);
    }

    /**
     * 获取Producer
     *
     * @param mqType
     * @param messageTypeCode
     */
    default <P> P getProducer(MqType mqType, String messageTypeCode) {
        Assert.hasText(messageTypeCode, () -> "MqManager messageTypeCode is null");
        MqManagerAction<P, ?> action = getManagerAction(mqType);
        return action.getProducer(messageTypeCode);
    }

    /**
     * 获取Consumer
     *
     * @param mqType
     * @param messageTypeCode
     */
    default <C> C getConsumer(MqType mqType, String messageTypeCode) {
        Assert.hasText(messageTypeCode, () -> "MqManager messageTypeCode is null");
        MqManagerAction<?, C> action = getManagerAction(mqType);
        return action.getConsumer(messageTypeCode);
    }

    /**
     * 获取支持的消息类型
     */
    default Set<String> getSupportMessageType(MqType mqType, Object messageListener) {
        Assert.notNull(messageListener, () -> "MqManager messageListener is null");
        MqManagerAction<?, ?> action = getManagerAction(mqType);
        return action.getSupportMessageType(messageListener);
    }

}
