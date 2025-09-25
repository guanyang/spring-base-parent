package io.github.guanyang.mq.config.support;

import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.mq.config.MqProperties;
import io.github.guanyang.mq.config.MqProperties.GlobalConfig;
import io.github.guanyang.mq.core.EventLogService;
import io.github.guanyang.mq.core.support.DefaultEventLogServiceImpl;
import io.github.guanyang.mq.core.support.EventMessageHandlerParseFactory;
import io.github.guanyang.mq.model.EventLogContext;
import io.github.guanyang.mq.model.EventMessageHandlerContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.BackOff;

import java.util.Collections;
import java.util.Optional;

import static io.github.guanyang.mq.model.MqType.KAFKA;


/**
 * @author guanyang
 */
@Slf4j
public class KafkaDefaultErrorHandler extends DefaultErrorHandler {

    public KafkaDefaultErrorHandler(ObjectProvider<MqProperties> properties, ObjectProvider<EventLogService> eventLogService) {
        super(createRecoverer(eventLogService), createBackOff(properties));
    }

    public static BackOff createBackOff(ObjectProvider<MqProperties> properties) {
        GlobalConfig globalConfig = Optional.ofNullable(properties.getIfAvailable()).map(MqProperties::getGlobalConfig).orElseGet(GlobalConfig::new);
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(globalConfig.getRetryTimes());
        backOff.setInitialInterval(globalConfig.getInitialInterval());
        backOff.setMultiplier(globalConfig.getMultiplier());
        backOff.setMaxInterval(globalConfig.getMaxInterval());
        return backOff;
    }

    public static ConsumerRecordRecoverer createRecoverer(ObjectProvider<EventLogService> eventLogService) {
        return (record, ex) -> {
            EventMessageHandlerContext context = EventMessageHandlerParseFactory.parse(KAFKA, record);
            if (!EventMessageHandlerContext.validate(context)) {
                log.warn("EventMessageHandlerContext validate fail: {}", record, ex);
                return;
            }
            Throwable cause = getCause(ex);
            EventLogContext.handleEventLog(Collections.singletonList(context.getEventMessage()), cause, ctx -> {
                EventLogService logService = eventLogService.getIfAvailable(DefaultEventLogServiceImpl::new);
                logService.batchSaveEventLog(ctx);
            });
        };
    }

    public static Throwable getCause(Throwable ex) {
        if (ex instanceof ListenerExecutionFailedException) {
            return Optional.ofNullable(ex.getCause()).orElse(ex);
        }
        return ex;
    }
}
