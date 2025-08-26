package org.gy.framework.mq.core.support;

import org.gy.framework.mq.model.IMessageType.DefaultMessageType;
import org.springframework.stereotype.Service;

/**
 * @author guanyang
 */
@Service
public class DefaultEventMessageProducerNormalServiceImpl extends AbstractEventMessageProducerService {
    @Override
    public String getMessageTypeCode() {
        return DefaultMessageType.NORMAL.getCode();
    }
}
