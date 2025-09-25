package io.github.guanyang.mq.core.support;

import org.springframework.util.Assert;

public class DefaultEventMessageProducerServiceImpl extends AbstractEventMessageProducerService {

    private final String messageTypeCode;

    public DefaultEventMessageProducerServiceImpl(String messageTypeCode) {
        Assert.hasText(messageTypeCode, () -> "messageTypeCode is required!");
        this.messageTypeCode = messageTypeCode;
    }

    @Override
    public String getMessageTypeCode() {
        return messageTypeCode;
    }

}
