package org.gy.framework.mq.core.support;

import com.alibaba.fastjson2.TypeReference;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.core.EventMessageConsumerService;
import org.gy.framework.mq.core.EventMessageProducerService;
import org.gy.framework.mq.model.IEventType;
import org.gy.framework.mq.model.IMessageType;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class EventMessageServiceManager implements Ordered {

    private static final ThreadLocal<EventMessageConsumerService<?, ?>> CURRENT_SERVICE = new TransmittableThreadLocal<>();

    public static boolean isCurrentServiceActive() {
        return getCurrentService() != null;
    }

    public static void setCurrentService(EventMessageConsumerService service) {
        CURRENT_SERVICE.set(service);
    }

    public static EventMessageConsumerService getCurrentService() {
        return CURRENT_SERVICE.get();
    }

    public static void clearCurrentService() {
        CURRENT_SERVICE.remove();
    }

    public static <T, R> EventMessageConsumerService<T, R> getService(IEventType eventType) {
        return CommonServiceManager.getService(EventMessageConsumerService.class, eventType);
    }

    public static <T, R> Optional<EventMessageConsumerService<T, R>> getServiceOptional(IEventType eventType) {
        return CommonServiceManager.getServiceOptional(new TypeReference<EventMessageConsumerService<T, R>>() {
        }, eventType);
    }

    public static EventMessageProducerService getSendService(IMessageType messageType) {
        return CommonServiceManager.getService(EventMessageProducerService.class, messageType);
    }

    public static Optional<EventMessageProducerService> getSendServiceOptional(IMessageType messageType) {
        return CommonServiceManager.getServiceOptional(EventMessageProducerService.class, messageType);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
