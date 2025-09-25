package io.github.guanyang.mq.core;


import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import io.github.guanyang.core.support.CommonServiceAction;
import io.github.guanyang.core.support.CommonServiceManager;
import io.github.guanyang.mq.model.EventMessage;
import io.github.guanyang.mq.model.IEventType;
import io.github.guanyang.mq.model.IMessageType;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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

    /**
     * 异步发送消息（消息自定义扩展）
     *
     * @param t          消息体
     * @param eventType  事件类型
     * @param customizer 消息自定义
     */
    default <T> void asyncSend(T t, IEventType eventType, Consumer<EventMessage<T>> customizer) {
        EventMessage<T> req = EventMessage.of(eventType, t);
        customizer.accept(req);
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
    @Override
    default void init() {
        CommonServiceManager.registerInstance(EventMessageProducerService.class, this, EventMessageProducerService::getMessageTypeCode);
    }

    static String getServiceName(Class<?> serviceClass, String messageTypeCode) {
        return StringUtils.joinWith(StrUtil.UNDERLINE, serviceClass.getSimpleName(), messageTypeCode);
    }

}
