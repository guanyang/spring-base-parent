package org.gy.framework.mq.core.support;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.lock.core.DistributedLock;
import org.gy.framework.lock.core.support.RedissonDistributedLock;
import org.gy.framework.mq.config.MqProperties;
import org.gy.framework.mq.core.EventLogService;
import org.gy.framework.mq.core.EventMessageConsumerService;
import org.gy.framework.mq.core.EventMessageDispatchService;
import org.gy.framework.mq.core.EventMessageHandler;
import org.gy.framework.mq.model.EventLogContext;
import org.gy.framework.mq.model.EventMessage;
import org.gy.framework.mq.model.EventMessageDispatchResult;
import org.gy.framework.mq.model.EventMessageHandlerContext;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class AbstractEventMessageHandler implements EventMessageHandler {

    public static final String DEFAULT_RETRY_KEY = "X-EventMessageHandler-RetryCount";

    @Resource
    protected MqProperties properties;

    @Resource
    protected EventLogService eventLogService;

    @Resource
    protected EventMessageDispatchService eventMessageDispatchService;

    @Autowired(required = false)
    protected RedissonClient redisson;

    protected abstract EventMessageHandlerContext parse(Object originalMsg, Object messageListener);

    protected EventMessageHandlerContext internalParse(Object originalMsg, Object messageListener, Predicate<Object> listenerPredicate, Function<Object, Set<String>> listenerSupportFunc) {
        if (messageListener == null || !listenerPredicate.test(messageListener)) {
            log.warn("[EventMessageHandler]消息监听器不支持: {}", messageListener);
            return EventMessageHandlerContext.NONE;
        }
        EventMessageHandlerContext context = EventMessageHandlerParseFactory.parse(getMqType(), originalMsg);
        if (!EventMessageHandlerContext.validate(context)) {
            return context;
        }
        EventMessage<?> eventMessage = context.getEventMessage();
        EventMessageConsumerService<?, ?> consumerService = context.getConsumerService();
        Set<String> supportMessageType = listenerSupportFunc.apply(messageListener);
        Set<String> messageTypeCode = Optional.ofNullable(consumerService.getMessageTypeCode()).orElseGet(Collections::emptySet);
        boolean messageSupport = messageTypeCode.stream().anyMatch(supportMessageType::contains);
        if (!messageSupport) {
            log.warn("[EventMessageHandler]监听器不支持此消息类型: event={},messageType={},supportMessageType={}", eventMessage.getEventTypeCode(), messageTypeCode, supportMessageType);
            return context;
        }
        context.setMessageListener(messageListener);
        context.setMessageSupport(messageSupport);
        return context;
    }


    @Override
    public void subscribe(Object originalMsg, Object messageListener) {
        EventMessageHandlerContext context = parse(originalMsg, messageListener);
        String msgId = Optional.ofNullable(context).map(EventMessageHandlerContext::getOriginalMsgId).orElseGet(null);
        EventMessage<?> eventMessage = Optional.ofNullable(context).map(EventMessageHandlerContext::getEventMessage).orElse(null);
        if (!EventMessageHandlerContext.support(context)) {
            log.warn("[EventMessageHandler]消息暂不支持处理: msgId={}, msgBody={}", msgId, eventMessage);
            return;
        }
        DistributedLock lock = internalLock(eventMessage);
        boolean lockFlag = Optional.ofNullable(lock).map(DistributedLock::tryLock).orElse(true);
        if (!lockFlag) {
            log.warn("[EventMessageHandler]消息已经处理: msgId={},msgBody={}", msgId, eventMessage);
            return;
        }
        log.info("[EventMessageHandler]消息数据: msgId={},msgBody={}", msgId, eventMessage);
        internalExecute(context, lock);
    }

    /**
     * 内部执行
     *
     * @param context
     * @param lock
     */
    protected void internalExecute(EventMessageHandlerContext context, DistributedLock lock) {
        EventLogContext<EventMessage<?>, Object> ctx = new EventLogContext<>();
        EventMessage<?> eventMessage = context.getEventMessage();
        ctx.setRequestId(eventMessage.getRequestId());
        ctx.setRequest(eventMessage);
        EventMessageDispatchResult dispatchResult = eventMessageDispatchService.execute(eventMessage);
        if (dispatchResult.hasException()) {
            //释放幂等key，抛出原异常，方便下次重试处理
            Optional.ofNullable(lock).ifPresent(DistributedLock::unlock);
            //异常重试处理
            internalException(context, dispatchResult);
            ctx.setEx(dispatchResult.getEx());
        } else {
            ctx.setResponse(dispatchResult.getResult());
        }
        // 保存事件日志（异步）
        internalEventLog(Collections.singletonList(ctx));
    }

    protected void internalEventLog(List<EventLogContext<EventMessage<?>, Object>> ctxList) {
        EventLogContext.handleEventLog(ctxList, eventLogService::batchSaveEventLog);
    }

    protected <T> void internalEventLog(List<EventMessage<T>> eventMessages, Throwable ex) {
        EventLogContext.handleEventLog(eventMessages, ex, eventLogService::batchSaveEventLog);
    }

    /**
     * 异常处理
     *
     * @param context
     * @param dispatchResult
     */
    @SneakyThrows
    protected void internalException(EventMessageHandlerContext context, EventMessageDispatchResult dispatchResult) {
        Throwable ex = dispatchResult.getEx();
        EventMessageConsumerService<?, ?> eventMessageService = context.getConsumerService();
        boolean supportRetry = Optional.ofNullable(eventMessageService).map(s -> s.supportRetry(ex)).orElse(false);
        if (!supportRetry) {
            log.warn("[MessageListener]业务异常暂不支持重试: msgId={},msgBody={}", context.getOriginalMsgId(), context.getEventMessage(), ex);
            return;
        }
        int maxRetryTimes = getRetryTimes();
        if (context.getCurrentRetryTimes() >= maxRetryTimes) {
            // 重试次数超过限制，不再处理，后续可以记录异常表或告警处理
            log.warn("[MessageListener]消息重试次数超过限制: msgId={},msgBody={},retryTimes={}", context.getOriginalMsgId(), context.getEventMessage(), maxRetryTimes, ex);
        } else {
            throw ex;
        }
    }


    protected DistributedLock internalLock(EventMessage<?> eventMessage) {
        if (redisson == null) {
            return null;
        }
        String idempotentKey = getUniqueKey(eventMessage);
        String code = String.valueOf(eventMessage.getEventTypeCode());
        String redisKey = StringUtils.joinWith(StrUtil.COLON, getIdempotentKeyPrefix(), code, idempotentKey);
        return new RedissonDistributedLock(redisson, redisKey, getIdempotentExpireMillis());
    }

    protected String getUniqueKey(EventMessage<?> eventMessage) {
        //如果没有传bizKey，则已requestId作为幂等key
        return Optional.ofNullable(eventMessage.getBizKey()).filter(StringUtils::isNotBlank).orElseGet(eventMessage::getRequestId);
    }

    protected int getRetryTimes() {
        return properties.getGlobalConfig().getRetryTimes();
    }

    protected long getIdempotentExpireMillis() {
        return properties.getGlobalConfig().getIdempotentExpireMillis();
    }

    protected String getIdempotentKeyPrefix() {
        return properties.getGlobalConfig().getIdempotentKeyPrefix();
    }

}
