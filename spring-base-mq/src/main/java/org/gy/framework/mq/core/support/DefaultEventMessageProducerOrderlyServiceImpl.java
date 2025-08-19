package org.gy.framework.mq.core.support;

import org.gy.framework.mq.model.IMessageType;
import org.gy.framework.mq.model.IMessageType.DefaultMessageType;
import org.springframework.stereotype.Service;

@Service
public class DefaultEventMessageProducerOrderlyServiceImpl extends AbstractEventMessageProducerService {
    @Override
    public IMessageType getMessageType() {
        return DefaultMessageType.ORDERLY;
    }
}
