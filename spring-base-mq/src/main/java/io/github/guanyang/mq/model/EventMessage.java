package io.github.guanyang.mq.model;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息事件封装
 *
 * @author gy
 */
@Data
public class EventMessage<T> implements Serializable {

    private static final long serialVersionUID = -8059102092489413340L;

    private String requestId = IdUtil.simpleUUID();

    /**
     * 事件毫秒时间戳
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 事件类型编码（必须）
     * @see IEventType
     */
    private String eventTypeCode;
    /**
     * 业务唯一标识，如果有则幂等处理
     */
    private String bizKey;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 延迟级别，18个等级（1~18），默认不延迟 <br/>
     * <p>
     * 延迟类别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    private int delayTimeLevel;

    /**
     * 顺序消费key，默认不设置
     */
    private String orderlyKey;

    /**
     * 消息标签
     */
    private String tag;

    public static <T> EventMessage<T> of(IEventType eventType, T data) {
        return of(eventType, data, null);
    }

    public static <T> EventMessage<T> of(IEventType eventType, T data, String bizKey) {
        EventMessage<T> eventMessage = new EventMessage<>();
        eventMessage.eventTypeCode = eventType.getCode();
        eventMessage.bizKey = bizKey;
        eventMessage.data = data;
        return eventMessage;
    }

    public void reset() {
        this.requestId = IdUtil.simpleUUID();
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
