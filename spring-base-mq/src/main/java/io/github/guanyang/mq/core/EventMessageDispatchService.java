package io.github.guanyang.mq.core;


import io.github.guanyang.mq.model.EventMessage;
import io.github.guanyang.mq.model.EventMessageDispatchResult;

/**
 * @author gy
 */
public interface EventMessageDispatchService {

    EventMessageDispatchResult execute(EventMessage<?> event);
}
