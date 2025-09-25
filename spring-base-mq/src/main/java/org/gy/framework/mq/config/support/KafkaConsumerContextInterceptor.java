package org.gy.framework.mq.config.support;

import cn.hutool.extra.spring.SpringUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.gy.framework.mq.core.TraceService;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author guanyang
 */
public class KafkaConsumerContextInterceptor implements RecordInterceptor<Object, Object> {

    private final TraceService traceService;

    public KafkaConsumerContextInterceptor() {
        this.traceService = SpringUtil.getBean(TraceService.class);
    }

    @Override
    public ConsumerRecord<Object, Object> intercept(ConsumerRecord<Object, Object> consumerRecord, Consumer<Object, Object> consumer) {
        String traceKey = traceService.getTraceKey();
        Header header = Optional.ofNullable(consumerRecord.headers().lastHeader(traceKey)).orElse(null);
        if (header != null) {
            traceService.setTrace(new String(header.value(), StandardCharsets.UTF_8));
        }
        return consumerRecord;
    }

    @Override
    public void afterRecord(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer) {
        traceService.clearTrace();
    }
}
