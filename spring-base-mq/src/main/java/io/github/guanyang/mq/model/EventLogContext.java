package io.github.guanyang.mq.model;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 事件日志上下文封装
 *
 * @author gy
 */
@Data
@Slf4j
public class EventLogContext<REQ extends EventMessage<?>, RES> implements Serializable {

    private static final long serialVersionUID = -3716537966923659221L;

    private String requestId;

    private REQ request;

    private RES response;

    private Throwable ex;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static <T, R> R handleWithLog(EventMessage<T> req, Function<EventMessage<T>, R> function, Consumer<List<EventLogContext<EventMessage<?>, Object>>> consumer) {
        Objects.requireNonNull(req, "EventMessage is required!");
        Objects.requireNonNull(function, "Function is required!");

        EventLogContext<EventMessage<?>, Object> ctx = new EventLogContext<>();
        ctx.setRequestId(req.getRequestId());
        ctx.setRequest(req);
        try {
            R response = function.apply(req);
            ctx.setResponse(response);
            return response;
        } catch (Throwable e) {
            ctx.setEx(e);
            throw e;
        } finally {
            handleEventLog(Collections.singletonList(ctx), consumer);
        }
    }

    public static <T> void handleEventLog(List<EventLogContext<EventMessage<?>, Object>> ctxList, Consumer<List<EventLogContext<EventMessage<?>, Object>>> consumer) {
        if (CollectionUtil.isEmpty(ctxList)) {
            log.warn("[handleEventLog]Param ctxList is empty");
            return;
        }
        Optional.ofNullable(consumer).ifPresent(c -> c.accept(ctxList));
    }

    public static <T> void handleEventLog(List<EventMessage<T>> eventMessages, Throwable ex, Consumer<List<EventLogContext<EventMessage<?>, Object>>> consumer) {
        if (CollectionUtil.isEmpty(eventMessages)) {
            log.warn("[handleEventLog]Param eventMessages is empty");
            return;
        }
        List<EventLogContext<EventMessage<?>, Object>> ctxList = eventMessages.stream().map(msg -> {
            EventLogContext<EventMessage<?>, Object> ctx = new EventLogContext<>();
            ctx.setRequestId(msg.getRequestId());
            ctx.setRequest(msg);
            ctx.setEx(ex);
            return ctx;
        }).collect(Collectors.toList());
        handleEventLog(ctxList, consumer);
    }

}
