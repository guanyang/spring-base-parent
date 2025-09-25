package io.github.guanyang.mq.model;

import lombok.Data;
import io.github.guanyang.mq.core.EventMessageConsumerService;

import java.io.Serializable;

@Data
public class EventMessageDispatchResult implements Serializable {
    private static final long serialVersionUID = 7377345643668821360L;

    private static final EventMessageDispatchResult NONE = new EventMessageDispatchResult();

    private Object result;

    private EventMessageConsumerService service;

    private Throwable ex;

    public boolean hasException() {
        return ex != null;
    }

    public static EventMessageDispatchResult of(Throwable ex) {
        EventMessageDispatchResult context = new EventMessageDispatchResult();
        context.ex = ex;
        return context;
    }

    public static EventMessageDispatchResult of(Object result, EventMessageConsumerService service) {
        EventMessageDispatchResult context = new EventMessageDispatchResult();
        context.result = result;
        context.service = service;
        return context;
    }

    public static EventMessageDispatchResult of(Throwable ex, EventMessageConsumerService service) {
        EventMessageDispatchResult context = new EventMessageDispatchResult();
        context.ex = ex;
        context.service = service;
        return context;
    }
}
