package org.gy.framework.mq.config.support;

import cn.hutool.extra.spring.SpringUtil;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.gy.framework.mq.core.TraceService;

import java.util.function.Consumer;

/**
 * @author gy
 */
public class SendCallbackWrapper implements SendCallback {

    private final SendCallback sendCallback;

    private final TraceService traceService;

    private final String traceId;

    private final transient Thread callThread;

    private SendCallbackWrapper(SendCallback sendCallback) {
        this.sendCallback = sendCallback;
        this.traceService = SpringUtil.getBean(TraceService.class);
        this.traceId = traceService.getTraceId();
        this.callThread = Thread.currentThread();
    }

    public static SendCallback of(SendCallback sendCallback) {
        return new SendCallbackWrapper(sendCallback);
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        traceWrap(sendResult, sendCallback::onSuccess);
    }

    @Override
    public void onException(Throwable e) {
        traceWrap(e, sendCallback::onException);
    }

    private <T> void traceWrap(T t, Consumer<T> consumer) {
        // 如果回调在当前线程，则直接执行
        if (callThread == Thread.currentThread()) {
            consumer.accept(t);
            return;
        }
        traceService.setTrace(traceId);
        try {
            consumer.accept(t);
        } finally {
            traceService.clearTrace();
        }
    }
}
