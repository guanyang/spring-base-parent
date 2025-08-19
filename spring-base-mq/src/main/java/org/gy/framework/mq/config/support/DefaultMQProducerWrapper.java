package org.gy.framework.mq.config.support;

import cn.hutool.extra.spring.SpringUtil;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.gy.framework.mq.core.TraceService;

import java.util.Collection;
import java.util.Optional;

/**
 * @author gy
 */
public class DefaultMQProducerWrapper extends DefaultMQProducer {

    private final TraceService traceService;


    public DefaultMQProducerWrapper(String producerGroup) {
        super(producerGroup);
        this.traceService = SpringUtil.getBean(TraceService.class);
    }

    public SendResult send(Message msg) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msg));
    }

    public SendResult send(Message msg, long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msg), timeout);
    }

    public void send(Message msg, SendCallback sendCallback) throws MQClientException, RemotingException, InterruptedException {
        super.send(wrapTrace(msg), wrapTrace(sendCallback));
    }

    public void send(Message msg, SendCallback sendCallback, long timeout) throws MQClientException, RemotingException, InterruptedException {
        super.send(wrapTrace(msg), wrapTrace(sendCallback), timeout);
    }

    public void sendOneway(Message msg) throws MQClientException, RemotingException, InterruptedException {
        super.sendOneway(wrapTrace(msg));
    }

    public SendResult send(Message msg, MessageQueue mq) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msg), mq);
    }

    public SendResult send(Message msg, MessageQueue mq, long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msg), mq, timeout);
    }

    public void send(Message msg, MessageQueue mq, SendCallback sendCallback) throws MQClientException, RemotingException, InterruptedException {
        super.send(wrapTrace(msg), mq, wrapTrace(sendCallback));
    }

    public void send(Message msg, MessageQueue mq, SendCallback sendCallback, long timeout) throws MQClientException, RemotingException, InterruptedException {
        super.send(wrapTrace(msg), mq, wrapTrace(sendCallback), timeout);
    }

    public void sendOneway(Message msg, MessageQueue mq) throws MQClientException, RemotingException, InterruptedException {
        super.sendOneway(wrapTrace(msg), mq);
    }

    public SendResult send(Message msg, MessageQueueSelector selector, Object arg) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msg), selector, arg);
    }

    public SendResult send(Message msg, MessageQueueSelector selector, Object arg, long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msg), selector, arg, timeout);
    }

    public void send(Message msg, MessageQueueSelector selector, Object arg, SendCallback sendCallback) throws MQClientException, RemotingException, InterruptedException {
        super.send(wrapTrace(msg), selector, arg, wrapTrace(sendCallback));
    }

    public void send(Message msg, MessageQueueSelector selector, Object arg, SendCallback sendCallback, long timeout) throws MQClientException, RemotingException, InterruptedException {
        super.send(wrapTrace(msg), selector, arg, wrapTrace(sendCallback), timeout);
    }

    public SendResult sendDirect(Message msg, MessageQueue mq, SendCallback sendCallback) throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        return super.sendDirect(wrapTrace(msg), mq, wrapTrace(sendCallback));
    }

    public SendResult sendByAccumulator(Message msg, MessageQueue mq, SendCallback sendCallback) throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        return super.sendByAccumulator(wrapTrace(msg), mq, wrapTrace(sendCallback));
    }

    public void sendOneway(Message msg, MessageQueueSelector selector, Object arg) throws MQClientException, RemotingException, InterruptedException {
        super.sendOneway(wrapTrace(msg), selector, arg);
    }

    public SendResult send(Collection<Message> msgs) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msgs));
    }

    public SendResult send(Collection<Message> msgs, long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msgs), timeout);
    }

    public SendResult send(Collection<Message> msgs, MessageQueue messageQueue) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msgs), messageQueue);
    }

    public void send(Collection<Message> msgs, SendCallback sendCallback, long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        super.send(wrapTrace(msgs), wrapTrace(sendCallback), timeout);
    }

    public SendResult send(Collection<Message> msgs, MessageQueue messageQueue, long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return super.send(wrapTrace(msgs), messageQueue, timeout);
    }

    public void send(Collection<Message> msgs, SendCallback sendCallback) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        super.send(wrapTrace(msgs), wrapTrace(sendCallback));
    }

    public void send(Collection<Message> msgs, MessageQueue mq, SendCallback sendCallback) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        super.send(wrapTrace(msgs), mq, wrapTrace(sendCallback));
    }

    public void send(Collection<Message> msgs, MessageQueue mq, SendCallback sendCallback, long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        super.send(wrapTrace(msgs), mq, wrapTrace(sendCallback), timeout);
    }

    private Collection<Message> wrapTrace(Collection<Message> msgs) {
        if (msgs == null) {
            return null;
        }
        msgs.forEach(this::wrapTrace);
        return msgs;
    }

    private static SendCallback wrapTrace(SendCallback sendCallback) {
        if (sendCallback == null) {
            return null;
        }
        if (sendCallback instanceof SendCallbackWrapper) {
            return sendCallback;
        }
        return SendCallbackWrapper.of(sendCallback);
    }

    private Message wrapTrace(Message msg) {
        if (msg == null) {
            return null;
        }
        wrapMessage(msg);
        return msg;
    }

    private void wrapMessage(Message msg) {
        String traceKey = traceService.getTraceKey();
        String value = msg.getUserProperty(traceKey);
        if (value == null) {
            Optional.ofNullable(traceService.getTraceId()).ifPresent(v -> msg.putUserProperty(traceKey, v));
        }
    }
}
