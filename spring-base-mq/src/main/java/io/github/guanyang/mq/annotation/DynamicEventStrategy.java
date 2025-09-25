package io.github.guanyang.mq.annotation;


import io.github.guanyang.mq.model.IEventType;
import io.github.guanyang.mq.model.IMessageType;
import io.github.guanyang.mq.model.IMessageType.MessageTypeCode;

import java.lang.annotation.*;

/**
 * 动态事件策略注解
 *
 * @author guanyang
 * @version 1.0.0
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicEventStrategy {

    /**
     * 事件类型编码
     *
     * @see IEventType
     * @see IEventType.DefaultEventType
     */
    String eventTypeCode();

    /**
     * 支持重试的异常
     */
    Class<? extends Throwable>[] supportRetry() default {Exception.class, Error.class};

    /**
     * 消息类型编码
     *
     * @see IMessageType
     * @see IMessageType.DefaultMessageType
     */
    String[] messageTypeCode() default {MessageTypeCode.DEFAULT_NORMAL};
}
