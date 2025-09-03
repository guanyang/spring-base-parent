package org.gy.framework.mq.config;

import org.gy.framework.core.support.CommonBoostrapAction;
import org.gy.framework.mq.model.MqType;

import java.util.Set;

public interface MqManagerAction<P, C> extends CommonBoostrapAction {

    /**
     * 获取MQ类型
     */
    MqType getMqType();

    /**
     * 获取支持的消息类型
     */
    Set<String> getSupportMessageType(Object messageListener);

    /**
     * 获取生产者
     */
    P getProducer(String messageTypeCode);

    /**
     * 获取消费者
     */
    C getConsumer(String messageTypeCode);
}
