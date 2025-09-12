package org.gy.framework.mq.core.support;

import com.alibaba.fastjson2.TypeReference;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.core.EventMessageConsumerService;
import org.gy.framework.mq.core.EventMessageProducerService;
import org.springframework.core.Ordered;

import java.util.Optional;

@Slf4j
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

    public static <T, R> EventMessageConsumerService<T, R> getService(String eventTypeCode) {
        return CommonServiceManager.getService(EventMessageConsumerService.class, eventTypeCode);
    }

    public static <T, R> Optional<EventMessageConsumerService<T, R>> getServiceOptional(String eventTypeCode) {
        return CommonServiceManager.getServiceOptional(new TypeReference<EventMessageConsumerService<T, R>>() {
        }, eventTypeCode);
    }

    public static EventMessageProducerService getSendService(String messageTypeCode) {
        return CommonServiceManager.getService(EventMessageProducerService.class, messageTypeCode);
    }

    public static Optional<EventMessageProducerService> getSendServiceOptional(String messageTypeCode) {
        return CommonServiceManager.getServiceOptional(EventMessageProducerService.class, messageTypeCode);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
