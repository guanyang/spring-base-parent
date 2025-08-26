package org.gy.framework.mq.core.support;

import org.gy.framework.mq.model.IMessageType;
import org.springframework.util.Assert;

public class DefaultRocketMQEventMessageProducerServiceImpl extends AbstractEventMessageProducerService {

    private final IMessageType messageType;

    public DefaultRocketMQEventMessageProducerServiceImpl(IMessageType messageType) {
        Assert.notNull(messageType, () -> "IMessageType is required!");
        this.messageType = messageType;
    }

    @Override
    public String getMessageTypeCode() {
        return messageType.getCode();
    }

}
