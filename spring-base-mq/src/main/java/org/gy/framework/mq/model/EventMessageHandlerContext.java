package org.gy.framework.mq.model;

import lombok.Data;
import org.gy.framework.mq.core.EventMessageConsumerService;

@Data
public class EventMessageHandlerContext {

    public static final EventMessageHandlerContext NONE = new EventMessageHandlerContext();
    /**
     * 原始消息ID
     */
    private String originalMsgId;
    /**
     * 原始消息
     */
    private Object originalMsg;
    /**
     * 解析的事件消息
     */
    private EventMessage<?> eventMessage;
    /**
     * 消费者事件服务
     */
    private EventMessageConsumerService<?, ?> consumerService;

    /**
     * 消息监听器
     */
    private Object messageListener;

    /**
     * 消息是否支持
     */
    private boolean messageSupport = false;

    /**
     * 当前重试次数
     */
    private int currentRetryTimes = 1;

    public static boolean validate(EventMessageHandlerContext context) {
        return context != null && context.getEventMessage() != null && context.getConsumerService() != null;
    }

    public static boolean support(EventMessageHandlerContext context) {
        return validate(context) && context.isMessageSupport();
    }

}
