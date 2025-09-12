package org.gy.framework.mq.core.support;

import lombok.extern.slf4j.Slf4j;
import org.gy.framework.mq.config.MqManager;
import org.gy.framework.mq.core.EventLogService;
import org.gy.framework.mq.core.EventMessageConsumerService;
import org.gy.framework.mq.core.EventMessageProducerService;
import org.gy.framework.mq.model.EventLogContext;
import org.gy.framework.mq.model.EventMessage;
import org.springframework.util.Assert;

import javax.annotation.Resource;
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
