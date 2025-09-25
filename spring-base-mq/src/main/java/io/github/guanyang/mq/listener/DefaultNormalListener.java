package io.github.guanyang.mq.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import io.github.guanyang.mq.model.MqType;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 默认普通消费监听器
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class DefaultNormalListener extends AbstractMessageListener implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            long startTime = System.currentTimeMillis();
            String msgId = msg.getMsgId();
            try {
                messageHandler(MqType.ROCKETMQ, msg, this);
            } catch (Throwable e) {
                String msgBody = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.error("[DefaultNormalListener]消费处理异常: msgId={},msgBody={}", msgId, msgBody, e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            log.info("[DefaultNormalListener]消费处理成功：msgId={},time={}ms", msgId, (System.currentTimeMillis() - startTime));
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
