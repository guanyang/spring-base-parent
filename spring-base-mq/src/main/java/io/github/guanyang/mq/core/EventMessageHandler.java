package io.github.guanyang.mq.core;

import io.github.guanyang.mq.model.EventMessage;
import io.github.guanyang.mq.model.MqType;

import java.util.List;

/**
 * 功能描述：消息处理器
 *
 * @author gy
 * @version 1.0.0
 */
public interface EventMessageHandler {

    /**
     * 获取MQ类型
     */
    MqType getMqType();

    /**
     * 发送消息
     *
     * @param messageTypeCode 消息类型编码
     * @param eventMessages   消息
     */
    <T> void publish(String messageTypeCode, List<EventMessage<T>> eventMessages);

    /**
     * 订阅消息
     *
     * @param originalMsg     原始消息
     * @param messageListener 监听器
     */
    void subscribe(Object originalMsg, Object messageListener);
}
