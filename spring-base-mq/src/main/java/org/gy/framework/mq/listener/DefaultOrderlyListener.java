package org.gy.framework.mq.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.gy.framework.mq.model.MqType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 默认顺序消费监听器
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Component("defaultOrderlyListener")
public class DefaultOrderlyListener extends AbstractMessageListener implements MessageListenerOrderly {
    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgList, ConsumeOrderlyContext context) {
        for (MessageExt msg : msgList) {
            long startTime = System.currentTimeMillis();
            String msgId = msg.getMsgId();
            try {
                messageHandler(MqType.ROCKETMQ, msg, this);
            } catch (Throwable e) {
                String msgBody = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.error("[DefaultOrderlyListener]消费处理异常: msgId={},msgBody={}", msgId, msgBody, e);
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
            log.info("[DefaultOrderlyListener]消费处理成功：msgId={},time={}ms", msgId, (System.currentTimeMillis() - startTime));
        }
        return ConsumeOrderlyStatus.SUCCESS;
    }
}
