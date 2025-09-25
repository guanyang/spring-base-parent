package io.github.guanyang.mq.core;


import io.github.guanyang.core.support.CommonServiceAction;
import io.github.guanyang.core.support.CommonServiceManager;
import io.github.guanyang.mq.core.support.EventMessageServiceManager;
import io.github.guanyang.mq.model.EventMessage;
import io.github.guanyang.mq.model.IEventType;
import io.github.guanyang.mq.model.IMessageType;

import java.util.Set;
import java.util.function.Function;

/**
 * @author gy
 */
public interface EventMessageConsumerService<T, R> extends CommonServiceAction {

    /**
     * 获取事件类型编码
     *
     * @see IEventType
     */
    String getEventTypeCode();

    /**
     * 执行事件消息处理
     *
     * @param eventMessage 事件消息对象
     * @return 处理结果
     */
    R execute(EventMessage<T> eventMessage);

    /**
     * 获取消息类型编码
     *
     * @see IMessageType
     */
    Set<String> getMessageTypeCode();

    /**
     * 是否支持重试
     *
     * @param ex 异常对象
     * @return true:支持重试
     */
    default boolean supportRetry(Throwable ex) {
        return ex instanceof Exception || ex instanceof Error;
    }

    /**
     * 初始化
     */
    default void init() {
        CommonServiceManager.registerInstance(EventMessageConsumerService.class, this, EventMessageConsumerService::getEventTypeCode);
    }

    default <REQ, RES> RES doWithContext(REQ req, Function<REQ, RES> function) {
        EventMessageServiceManager.setCurrentService(this);
        try {
            return function.apply(req);
        } finally {
            EventMessageServiceManager.clearCurrentService();
        }
    }
}
