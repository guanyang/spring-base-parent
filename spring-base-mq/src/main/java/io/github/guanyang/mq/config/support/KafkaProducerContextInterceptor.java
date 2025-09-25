package io.github.guanyang.mq.config.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import io.github.guanyang.mq.core.TraceService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class KafkaProducerContextInterceptor implements ProducerInterceptor<Object, Object> {

    private final TraceService traceService;

    public KafkaProducerContextInterceptor() {
        this.traceService = SpringUtil.getBean(TraceService.class);
    }

    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> producerRecord) {
        wrapTrace(producerRecord, traceService);
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }

    private static <K, V> void wrapTrace(ProducerRecord<K, V> message, TraceService traceService) {
        String traceKey = traceService.getTraceKey();
        Optional.ofNullable(traceService.getTraceId()).ifPresent(v -> message.headers().add(traceKey, v.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 向 props 添加自定义的 ProducerInterceptor，避免重复
     */
    public static void addInterceptor(Map<String, Object> props, Class<? extends ProducerInterceptor<?, ?>> interceptorClass) {
        Object existing = props.get(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG);
        Set<String> interceptors = new LinkedHashSet<>();
        if (existing instanceof String) {
            // 配置是逗号分隔的 className
            Stream.of(((String) existing).split(StrUtil.COMMA)).forEach(interceptors::add);
        } else if (existing instanceof List) {
            // 配置是 List<ClassName>
            ((List<?>) existing).stream().map(Object::toString).forEach(interceptors::add);
        }
        // 添加自定义的 interceptor
        interceptors.add(interceptorClass.getName());
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, new ArrayList<>(interceptors));
    }
}
