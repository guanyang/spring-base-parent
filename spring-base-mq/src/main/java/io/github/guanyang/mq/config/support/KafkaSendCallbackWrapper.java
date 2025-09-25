package io.github.guanyang.mq.config.support;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.util.concurrent.FutureCallback;
import io.github.guanyang.mq.core.TraceService;

import java.util.function.Consumer;

public class KafkaSendCallbackWrapper<T> implements FutureCallback<T> {

    private final FutureCallback<T> sendCallback;

    private final TraceService traceService;

    private final String traceId;

    private final transient Thread callThread;

    public KafkaSendCallbackWrapper(FutureCallback<T> sendCallback) {
        this.sendCallback = sendCallback;
        this.traceService = SpringUtil.getBean(TraceService.class);
        this.traceId = traceService.getTraceId();
        this.callThread = Thread.currentThread();
    }

    public static <T> FutureCallback<T> of(FutureCallback<T> sendCallback) {
        if (sendCallback == null) {
            return null;
        }
        if (sendCallback instanceof KafkaSendCallbackWrapper) {
            return sendCallback;
        }
        return new KafkaSendCallbackWrapper<>(sendCallback);
    }

    @Override
    public void onFailure(Throwable ex) {
        traceWrap(ex, sendCallback::onFailure);
    }

    @Override
    public void onSuccess(T result) {
        traceWrap(result, sendCallback::onSuccess);
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
