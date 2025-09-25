package io.github.guanyang.mq.core.support;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.rocketmq.common.message.MessageExt;
import io.github.guanyang.mq.core.EventMessageConsumerService;
import io.github.guanyang.mq.model.EventMessage;
import io.github.guanyang.mq.model.EventMessageHandlerContext;
import io.github.guanyang.mq.model.MqType;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static io.github.guanyang.mq.model.MqType.KAFKA;
import static io.github.guanyang.mq.model.MqType.ROCKETMQ;

@Slf4j
public class EventMessageHandlerParseFactory {

    public static EventMessageHandlerContext parse(MqType mqType, Object originalMsg) {
        switch (mqType) {
            case ROCKETMQ:
                return parseForRocketMq(originalMsg);
            case KAFKA:
                return parseForKafka(originalMsg);
            default:
                log.warn("[EventMessageHandler]消息类型不支持: {}", mqType);
                return EventMessageHandlerContext.NONE;
        }
    }

    /**
     * 解析Kafka原始消息
     *
     * @param originalMsg 原始消息对象
     * @return 解析后的上下文
     */
    private static EventMessageHandlerContext parseForKafka(Object originalMsg) {
        if (!(originalMsg instanceof ConsumerRecord)) {
            log.warn("[EventMessageHandler]KAFKA原始消息类型不支持: {}", originalMsg);
            return EventMessageHandlerContext.NONE;
        }
        ConsumerRecord<?, ?> record = (ConsumerRecord<?, ?>) originalMsg;
        String msgId = getOriginalMsgId(record);
        String msgBody = Optional.ofNullable(record.value()).map(Object::toString).orElse(null);
        return parseEventMessage(msgId, originalMsg, msgBody, KAFKA);
    }

    private static String getOriginalMsgId(ConsumerRecord<?, ?> record) {
        return StringUtils.joinWith(StrUtil.UNDERLINE, record.topic(), record.partition(), record.offset());
    }

    /**
     * 解析RocketMQ原始消息
     *
     * @param originalMsg 原始消息对象
     * @return 解析后的上下文
     */
    private static EventMessageHandlerContext parseForRocketMq(Object originalMsg) {
        if (!(originalMsg instanceof MessageExt)) {
            log.warn("[EventMessageHandler]ROCKETMQ原始消息类型不支持: {}", originalMsg);
            return EventMessageHandlerContext.NONE;
        }
        MessageExt msg = (MessageExt) originalMsg;
        String msgId = msg.getMsgId();
        String msgBody = new String(msg.getBody(), StandardCharsets.UTF_8);
        return parseEventMessage(msgId, originalMsg, msgBody, ROCKETMQ);
    }

    /**
     * 通用的消息解析逻辑
     *
     * @param msgId       消息ID
     * @param originalMsg 原始消息对象
     * @param msgBody     消息体内容
     * @param mqType      消息队列类型（用于日志）
     * @return 解析后的上下文
     */
    private static EventMessageHandlerContext parseEventMessage(String msgId, Object originalMsg, String msgBody, MqType mqType) {
        EventMessageHandlerContext context = new EventMessageHandlerContext();
        context.setOriginalMsgId(msgId);
        context.setOriginalMsg(originalMsg);

        EventMessage<?> eventMessage = JSON.parseObject(msgBody, EventMessage.class);
        if (eventMessage == null || eventMessage.getEventTypeCode() == null) {
            log.warn("[EventMessageHandler]{}消息参数错误: msgId={},msgBody={}", mqType, msgId, msgBody);
            return context;
        }
        context.setEventMessage(eventMessage);

        EventMessageConsumerService<?, ?> actionService = EventMessageServiceManager.getServiceOptional(eventMessage.getEventTypeCode()).orElse(null);
        if (actionService == null || actionService.getEventTypeCode() == null) {
            log.warn("[EventMessageHandler]{}消息事件服务无效: event={}", mqType, eventMessage.getEventTypeCode());
            return context;
        }
        context.setConsumerService(actionService);
        return context;
    }
}
