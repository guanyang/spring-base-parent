package io.github.guanyang.mq.core.support;


import com.alibaba.fastjson2.util.TypeUtils;
import io.github.guanyang.mq.model.DynamicEventContext;
import io.github.guanyang.mq.model.IMessageType;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public String getEventTypeCode() {
        return context.getEventType().getCode();
    }

    @Override
    public Set<String> getMessageTypeCode() {
        Set<IMessageType> messageTypes = Optional.ofNullable(context.getMessageTypes()).orElseGet(Collections::emptySet);
        return messageTypes.stream().map(IMessageType::getCode).collect(Collectors.toSet());
    }

    @Override
    public boolean supportRetry(Throwable ex) {
        return context.getSupportRetry().test(ex);
    }
}
