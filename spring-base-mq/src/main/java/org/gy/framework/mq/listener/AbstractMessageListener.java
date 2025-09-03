package org.gy.framework.mq.listener;

import lombok.extern.slf4j.Slf4j;
import org.gy.framework.mq.config.MqManager;
import org.gy.framework.mq.model.MqType;

import javax.annotation.Resource;


/**
 * @author gy
 */
@Slf4j
public abstract class AbstractMessageListener {

    @Resource
    private MqManager mqManager;

    /**
     * 订阅消息
     *
     * @param mqType          mq类型
     * @param originalMsg     原始消息
     * @param messageListener 消息监听器
     */
    protected void messageHandler(MqType mqType, Object originalMsg, Object messageListener) {
        mqManager.subscribe(mqType, originalMsg, messageListener);
    }

}
