package org.gy.framework.mq.core;


import org.gy.framework.mq.model.EventMessage;
import org.gy.framework.mq.model.EventMessageDispatchResult;

/**
 * @author gy
 */
public interface EventMessageDispatchService {

    EventMessageDispatchResult execute(EventMessage<?> event);
}
