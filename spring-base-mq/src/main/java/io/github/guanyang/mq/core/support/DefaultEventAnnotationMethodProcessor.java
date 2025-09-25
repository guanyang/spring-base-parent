package io.github.guanyang.mq.core.support;

import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.core.support.CommonServiceManager;
import io.github.guanyang.mq.annotation.DynamicEventStrategy;
import io.github.guanyang.mq.core.EventLogService;
import io.github.guanyang.mq.model.DynamicEventContext;
import io.github.guanyang.mq.model.IEventType;
import io.github.guanyang.mq.model.IMessageType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class DefaultEventAnnotationMethodProcessor extends AbstractEventAnnotationMethodProcessor<DynamicEventStrategy> {

    public DefaultEventAnnotationMethodProcessor(ObjectProvider<EventLogService> eventLogService) {
        super(eventLogService);
    }

    @Override
    public Class<DynamicEventStrategy> getAnnotationClass() {
        return DynamicEventStrategy.class;
    }

    @Override
    protected String getEventTypeCode(DynamicEventStrategy annotation) {
        return annotation.eventTypeCode();
    }

    @Override
    protected void eventContextCustomizer(DynamicEventContext<Object, Object> ctx, Method method, DynamicEventStrategy annotation) {
        Predicate<Throwable> supportRetry = DynamicEventContext.getRetryPredicate(annotation.supportRetry());
        ctx.setSupportRetry(supportRetry);

        String eventTypeCode = annotation.eventTypeCode();
        IEventType eventType = CommonServiceManager.getServiceOptional(IEventType.class, eventTypeCode).orElse(null);
        Assert.notNull(eventType, () -> "IEventType code not registered: " + eventTypeCode);
        ctx.setEventType(eventType);

        Set<IMessageType> messageTypes = Stream.of(annotation.messageTypeCode()).map(messageTypeCode -> {
            IMessageType messageType = CommonServiceManager.getServiceOptional(IMessageType.class, messageTypeCode).orElse(null);
            Assert.notNull(messageType, () -> "IMessageType code not registered: " + messageTypeCode);
            return messageType;
        }).collect(Collectors.toSet());
        ctx.setMessageTypes(messageTypes);
    }

}
