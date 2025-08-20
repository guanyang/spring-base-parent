package org.gy.framework.mq.core.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.core.exception.BizException;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.core.EventMessageConsumerService;
import org.gy.framework.mq.core.EventMessageDispatchService;
import org.gy.framework.mq.model.EventMessage;
import org.gy.framework.mq.model.EventMessageDispatchResult;

@Slf4j
public class DefaultEventMessageDispatchServiceImpl implements EventMessageDispatchService {
    @Override
    public EventMessageDispatchResult execute(EventMessage<?> event) {
        if (event == null || StringUtils.isBlank(event.getEventTypeCode())) {
            log.warn("[EventMessageDispatchService]消息数据为空, event={}", event);
            return EventMessageDispatchResult.of(new BizException(400, "Event data is empty!"));
        }
        EventMessageConsumerService actionService = EventMessageServiceManager.getServiceOptional(event.getEventTypeCode()).orElse(null);
        if (actionService == null) {
            log.warn("[EventMessageDispatchService]消息事件服务不存在:event={}", event);
            return EventMessageDispatchResult.of(new BizException(400, "Event service is empty!"));
        }
        EventMessageDispatchResult response = new EventMessageDispatchResult();
        response.setService(actionService);
        try {
            Object result = actionService.execute(event);
            response.setResult(result);
        } catch (Throwable e) {
            response.setEx(e);
        }
        return response;
    }
}
