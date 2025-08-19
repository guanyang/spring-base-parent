package org.gy.framework.mq.core.support;


import com.alibaba.fastjson2.util.TypeUtils;
import org.gy.framework.mq.model.DynamicEventContext;
import org.gy.framework.mq.model.IEventType;
import org.gy.framework.mq.model.IMessageType;

import java.lang.reflect.Type;
import java.util.Objects;

public class DefaultDynamicEventMessageServiceImpl extends AbstractEventMessageConsumerService<Object, Object> {

    private final DynamicEventContext context;

    public DefaultDynamicEventMessageServiceImpl(DynamicEventContext context) {
        this.context = Objects.requireNonNull(context, "DynamicEventContext is required!");
    }

    @Override
    protected Class<Object> getDataType() {
        return (Class<Object>) TypeUtils.getClass(context.getDataTypeReference());
    }

    @Override
    protected Type getDataTypeReference() {
        return context.getDataTypeReference();
    }

    @Override
    protected Object internalExecute(Object data) {
        return context.getExecuteFunction().apply(data);
    }

    @Override
    public IEventType getEventType() {
        return context.getEventType();
    }

    @Override
    public IMessageType getMessageType() {
        return context.getMessageType();
    }

    @Override
    public boolean supportRetry(Throwable ex) {
        return context.getSupportRetry().test(ex);
    }
}
