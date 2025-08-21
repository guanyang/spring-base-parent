package org.gy.framework.mq.core.support;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.gy.framework.mq.config.RocketMQProperties;
import org.gy.framework.mq.config.RocketMqManager;
import org.gy.framework.mq.config.RocketMqProducer;
import org.gy.framework.mq.core.EventLogService;
import org.gy.framework.mq.core.EventMessageProducerService;
import org.gy.framework.mq.model.EventLogContext;
import org.gy.framework.mq.model.EventMessage;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractEventMessageProducerService implements EventMessageProducerService {

    @Resource
    private RocketMqManager rocketMqManager;

    @Resource
    private EventLogService eventLogService;

    protected RocketMqProducer getProducer() {
        return rocketMqManager.getProducer(getMessageTypeCode());
    }

    @Override
    public <T> void asyncSend(List<EventMessage<T>> eventSendReqs) {
        sendInternalAsync(getProducer(), eventSendReqs);
    }

    protected <T> void sendInternalAsync(RocketMqProducer producer, List<EventMessage<T>> eventMessages) {
        if (CollectionUtil.isEmpty(eventMessages) || producer == null) {
            log.warn("[EventMessageSendService]参数错误");
            return;
        }
        try {
            sendInternal(producer, eventMessages, true);
        } catch (Exception e) {
            log.error("[EventMessageSendService]发送异常", e);
            EventLogContext.handleEventLog(eventMessages, e, eventLogService::batchSaveEventLog);
        }
    }

    protected <T> SendResult sendInternal(RocketMqProducer rocketMqProducer, List<EventMessage<T>> eventMessages, boolean async) throws Exception {
        RocketMQProperties properties = rocketMqProducer.getRocketMQProperties();
        DefaultMQProducer defaultProducer = rocketMqProducer.getProducer();
        SendCallback callback = async ? buildCallback(eventMessages) : null;
        //由于rocketmq批量消息不支持有序，不支持延迟，需要单独处理
        if (eventMessages.size() > 1) {
            List<Message> msgList = buildMsg(eventMessages, properties.getTopic());
            return sendInternal(defaultProducer, msgList, callback);
        } else {
            EventMessage<T> eventMessage = eventMessages.get(0);
            Message message = buildMsg(eventMessage, properties.getTopic());
            return sendInternal(defaultProducer, message, callback, eventMessage.getOrderlyKey());
        }
    }

    protected <T> SendResult sendInternal(DefaultMQProducer defaultProducer, Collection<Message> msgList, SendCallback callback) throws Exception {
        if (callback == null) {
            return defaultProducer.send(msgList);
        }
        defaultProducer.send(msgList, callback);
        return null;
    }

    protected SendResult sendInternal(DefaultMQProducer defaultProducer, Message msg, SendCallback callback, String orderlyKey) throws Exception {
        if (callback == null) {
            return StringUtils.isNotBlank(orderlyKey) ? defaultProducer.send(msg, new SelectMessageQueueByHash(), orderlyKey) : defaultProducer.send(msg);
        }
        if (StringUtils.isNotBlank(orderlyKey)) {
            defaultProducer.send(msg, new SelectMessageQueueByHash(), orderlyKey, callback);
        } else {
            defaultProducer.send(msg, callback);
        }
        return null;
    }

    protected <T> List<Message> buildMsg(Collection<EventMessage<T>> eventMessages, String topic) {
        return eventMessages.stream().map(eventMessage -> buildMsg(eventMessage, topic)).collect(Collectors.toList());
    }

    protected <T> Message buildMsg(EventMessage<T> eventMessage, String topic) {
        Message msg = new Message();
        msg.setTopic(topic);
        msg.setBody(JSON.toJSONBytes(eventMessage));
        if (eventMessage.getDelayTimeLevel() > 0) {
            msg.setDelayTimeLevel(eventMessage.getDelayTimeLevel());
        }
        if (StringUtils.isNotBlank(eventMessage.getTag())){
            msg.setTags(eventMessage.getTag());
        }
        return msg;
    }

    protected <T> SendCallback buildCallback(List<EventMessage<T>> eventMessages) {
        return new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("[EventMessageSendService]发送成功：result={}", sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.error("[EventMessageSendService]发送失败", e);
                EventLogContext.handleEventLog(eventMessages, e, eventLogService::batchSaveEventLog);
            }
        };
    }
}
