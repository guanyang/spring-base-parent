package io.github.guanyang.core.event;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public interface MessageQueueEventI extends EventI {

    /**
     * 事件类型
     */
    String getEventType();

    /**
     * 事件topic
     */
    String getEventTopic();

}
