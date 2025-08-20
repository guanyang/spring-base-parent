package org.gy.framework.mq.core;


import org.gy.framework.core.support.CommonServiceAction;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.model.EventMessage;
import org.gy.framework.mq.model.IEventType;
import org.gy.framework.mq.model.IMessageType;

import java.util.Collections;
import java.util.List;

/**
 * @author gy
 */
public interface EventMessageProducerService extends CommonServiceAction {

    /**
     * 消息类型编码
     *
     * @see IMessageType
     */
    String getMessageTypeCode();

    /**
     * 异步发送消息（普通消息）
     *
     * @param t         消息体
     * @param eventType 事件类型
     */
    default <T> void asyncSend(T t, IEventType eventType) {
        EventMessage<T> req = EventMessage.of(eventType, t);
        asyncSend(req);
    }

    /**
     * 异步发送消息（延时消息）
     *
     * @param t              消息体
     * @param eventType      事件类型
     * @param delayTimeLevel 延时等级, 参考：EventSendReq#delayTimeLevel
     */
    default <T> void asyncSend(T t, IEventType eventType, int delayTimeLevel) {
        EventMessage<T> req = EventMessage.of(eventType, t);
        req.setDelayTimeLevel(delayTimeLevel);
        asyncSend(req);
    }

    /**
     * 异步发送消息（顺序消息）
     *
     * @param t          消息体
     * @param eventType  事件类型
     * @param orderlyKey 顺序消费key
     */
    default <T> void asyncSend(T t, IEventType eventType, String orderlyKey) {
        EventMessage<T> req = EventMessage.of(eventType, t);
        req.setOrderlyKey(orderlyKey);
        asyncSend(req);
    }

    /*
     * 异步发送消息
     */
    default <T> void asyncSend(EventMessage<T> req) {
        asyncSend(Collections.singletonList(req));
    }

    /**
     * 异步批量发送消息（批量消息不支持延迟、顺序消费）
     */
    <T> void asyncSend(List<EventMessage<T>> reqs);

    /**
     * 直连处理
     */
    default <T, R> R directHandle(EventMessage<T> req) {
        throw new UnsupportedOperationException();
    }

    /**
     * 初始化
     */
    default void init() {
        CommonServiceManager.registerInstance(EventMessageProducerService.class, this, EventMessageProducerService::getMessageTypeCode);
    }

}
