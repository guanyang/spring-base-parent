package org.gy.framework.mq.core.support;

import org.springframework.util.Assert;

public class DefaultRocketMQEventMessageProducerServiceImpl extends AbstractEventMessageProducerService {

    private final String messageTypeCode;

    public DefaultRocketMQEventMessageProducerServiceImpl(String messageTypeCode) {
        Assert.hasText(messageTypeCode, () -> "messageTypeCode is required!");
        this.messageTypeCode = messageTypeCode;
    }

    @Override
    public String getMessageTypeCode() {
        return messageTypeCode;
    }

}
