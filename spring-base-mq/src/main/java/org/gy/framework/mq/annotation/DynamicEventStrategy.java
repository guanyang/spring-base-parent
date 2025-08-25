package org.gy.framework.mq.annotation;


import org.gy.framework.mq.model.IEventType;
import org.gy.framework.mq.model.IMessageType;
import org.gy.framework.mq.model.IMessageType.MessageTypeCode;

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
     * @see org.gy.framework.mq.model.IEventType.DefaultEventType
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
     * @see org.gy.framework.mq.model.IMessageType.DefaultMessageType
     */
    String[] messageTypeCode() default {MessageTypeCode.DEFAULT_NORMAL};
}
