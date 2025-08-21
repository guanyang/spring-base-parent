package org.gy.framework.mq.core;

public interface TraceService {

    /**
     * 获取链路追踪Key
     */
    String getTraceKey();

    /**
     * 获取链路追踪Id
     */
    String getTraceId();

    /**
     * 设置链路追踪Id
     */
    void setTrace(String traceId);

    /**
     * 清除链路追踪Id
     */
    void clearTrace();
}
