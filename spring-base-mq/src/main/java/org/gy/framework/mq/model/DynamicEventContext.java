package org.gy.framework.mq.model;

import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@Data
public class DynamicEventContext<T, R> implements Serializable {
    private static final long serialVersionUID = -3604769601257565437L;
    public static final Predicate<Throwable> DEFAULT_PREDICATE = ex -> ex instanceof Exception || ex instanceof Error;

    /**
     * 事件类型
     */
    private IEventType eventType;
    /**
     * 事件数据类型，支持嵌套泛型
     */
    private Type dataTypeReference;
    /**
     * 执行函数
     */
    private Function<T, R> executeFunction;

    /**
     * 默认支持异常重试
     */
    private Predicate<Throwable> supportRetry;
    /**
     * 消息类型
     */
    private IMessageType messageType;

    public DynamicEventContext(IEventType eventType, Type dataTypeReference, Function<T, R> executeFunction, Predicate<Throwable> supportRetry, IMessageType messageType) {
        this.eventType = Objects.requireNonNull(eventType, "eventType is required!");
        this.dataTypeReference = Objects.requireNonNull(dataTypeReference, "dataTypeReference is required!");
        this.executeFunction = Objects.requireNonNull(executeFunction, "executeFunction is required!");
        this.supportRetry = Objects.requireNonNull(supportRetry, "supportRetry is required!");
        this.messageType = Objects.requireNonNull(messageType, "messageType is required!");
    }

    public static Predicate<Throwable> getRetryPredicate(Class<? extends Throwable>[] retryTypes) {
        if (ArrayUtils.isEmpty(retryTypes)) {
            return DynamicEventContext.DEFAULT_PREDICATE;
        }
        return Arrays.stream(retryTypes).map(type -> (Predicate<Throwable>) type::isInstance).reduce(Predicate::or).orElse(t -> false);
    }
}
