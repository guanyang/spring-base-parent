package io.github.guanyang.mq.core.support;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.mq.config.MqManager;
import io.github.guanyang.mq.core.EventLogService;
import io.github.guanyang.mq.core.EventMessageConsumerService;
import io.github.guanyang.mq.core.EventMessageProducerService;
import io.github.guanyang.mq.model.EventLogContext;
import io.github.guanyang.mq.model.EventMessage;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
public abstract class AbstractEventMessageProducerService implements EventMessageProducerService {

    @Resource
    protected MqManager mqManager;

    @Resource
    protected EventLogService eventLogService;

    @Override
    public <T> void asyncSend(List<EventMessage<T>> eventSendReqs) {
        mqManager.publish(getMessageTypeCode(), eventSendReqs);
    }

    @Override
    public <T, R> R directHandle(EventMessage<T> req) {
        Assert.notNull(req, () -> "EventMessage is required!");
        EventMessageConsumerService<T, R> service = EventMessageServiceManager.getService(req.getEventTypeCode());
        return EventLogContext.handleWithLog(req, service::execute, eventLogService::batchSaveEventLog);
    }

}
