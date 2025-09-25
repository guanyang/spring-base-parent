package io.github.guanyang.mq.core.support;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import io.github.guanyang.mq.config.KafkaManager;
import io.github.guanyang.mq.config.KafkaProducer;
import io.github.guanyang.mq.config.MqProperties.KafkaProperty;
import io.github.guanyang.mq.config.support.KafkaSendCallbackWrapper;
import io.github.guanyang.mq.model.EventMessage;
import io.github.guanyang.mq.model.EventMessageHandlerContext;
import io.github.guanyang.mq.model.MqType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.SendResult;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Slf4j
public class DefaultKafkaMessageHandler extends AbstractEventMessageHandler {

    private final KafkaManager kafkaManager;

    public DefaultKafkaMessageHandler(KafkaManager kafkaManager) {
        this.kafkaManager = kafkaManager;
    }

    @Override
    public MqType getMqType() {
        return MqType.KAFKA;
    }

    @Override
    public <T> void publish(String messageTypeCode, List<EventMessage<T>> eventMessages) {
        KafkaProducer producer = kafkaManager.getProducer(messageTypeCode);
        sendInternalAsync(producer, eventMessages);
    }

    @Override
    protected EventMessageHandlerContext parse(Object originalMsg, Object messageListener) {
        Predicate<Object> listenerPredicate = listener -> listener instanceof AcknowledgingMessageListener;
        return internalParse(originalMsg, messageListener, listenerPredicate, kafkaManager::getSupportMessageType);
    }

    protected <T> void sendInternalAsync(KafkaProducer producer, List<EventMessage<T>> eventMessages) {
        if (CollectionUtil.isEmpty(eventMessages) || producer == null) {
            log.warn("[DefaultKafkaMessageHandler]参数错误");
            return;
        }
        sendInternal(producer, eventMessages);
    }

    protected <T> void sendInternal(KafkaProducer producer, List<EventMessage<T>> eventMessages) {
        KafkaProperty properties = producer.getKafkaProperty();
        KafkaTemplate<Object, Object> kafkaTemplate = producer.getProducer();
        eventMessages.forEach(msg -> sendInternal(kafkaTemplate, properties.getTopic(), msg));
    }

    protected <T> void sendInternal(KafkaTemplate<Object, Object> kafkaTemplate, String topic, EventMessage<T> eventMessage) {
        try {
            CompletableFuture<SendResult<Object, Object>> listenableFuture;
            String value = JSON.toJSONString(eventMessage);
            if (StringUtils.isNotBlank(eventMessage.getOrderlyKey())) {
                listenableFuture = kafkaTemplate.send(topic, eventMessage.getOrderlyKey(), value);
            } else {
                listenableFuture = kafkaTemplate.send(topic, value);
            }
            addCallback(listenableFuture, buildCallback(Collections.singletonList(eventMessage)));
        } catch (Throwable e) {
            internalEventLog(Collections.singletonList(eventMessage), e);
        }
    }

    protected <T> void addCallback(CompletableFuture<T> future, FutureCallback<T> callback) {
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                callback.onSuccess(result);
            } else {
                callback.onFailure(ex);
            }
        });
    }

    protected <T, K, V> FutureCallback<SendResult<K, V>> buildCallback(List<EventMessage<T>> eventMessages) {
        return KafkaSendCallbackWrapper.of(new FutureCallback<>() {
            @Override
            public void onSuccess(SendResult<K, V> result) {
                log.info("[DefaultKafkaMessageHandler]发送成功：recordMetadata={}", result.getRecordMetadata());
            }

            @Override
            public void onFailure(Throwable ex) {
                internalEventLog(eventMessages, ex);
            }
        });
    }
}
