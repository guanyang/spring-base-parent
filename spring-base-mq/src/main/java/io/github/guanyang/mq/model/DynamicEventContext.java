package io.github.guanyang.mq.model;

import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
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
    private Set<IMessageType> messageTypes;

    public void contextCheck() {
        Assert.notNull(eventType, () -> "eventType is required!");
        Assert.notNull(dataTypeReference, () -> "dataTypeReference is required!");
        Assert.notNull(executeFunction, () -> "executeFunction is required!");
        Assert.notNull(supportRetry, () -> "supportRetry is required!");
        Assert.notEmpty(messageTypes, () -> "messageTypes is required!");
    }

    public static Predicate<Throwable> getRetryPredicate(Class<? extends Throwable>[] retryTypes) {
        if (ArrayUtils.isEmpty(retryTypes)) {
            return DynamicEventContext.DEFAULT_PREDICATE;
        }
        return Arrays.stream(retryTypes).map(type -> (Predicate<Throwable>) type::isInstance).reduce(Predicate::or).orElse(t -> false);
    }
}
