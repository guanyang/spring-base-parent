package org.gy.framework.mq.core;


import org.gy.framework.core.support.CommonServiceAction;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.model.EventMessage;
import org.gy.framework.mq.model.IEventType;
import org.gy.framework.mq.model.IMessageType;

/**
 * @author gy
 */
public interface EventMessageConsumerService<T, R> extends CommonServiceAction {

    IEventType getEventType();

    R execute(EventMessage<T> eventMessage);

    IMessageType getMessageType();

    default boolean supportRetry(Throwable ex) {
        return ex instanceof Exception || ex instanceof Error;
    }

    default void init() {
        CommonServiceManager.registerInstance(EventMessageConsumerService.class, this, EventMessageConsumerService::getEventType);
    }
}
