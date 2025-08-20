package org.gy.framework.mq.listener;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.common.message.MessageExt;
import org.gy.framework.lock.core.DistributedLock;
import org.gy.framework.lock.core.support.RedissonDistributedLock;
import org.gy.framework.mq.config.RocketMqManager;
import org.gy.framework.mq.core.EventLogService;
import org.gy.framework.mq.core.EventMessageConsumerService;
import org.gy.framework.mq.core.EventMessageDispatchService;
import org.gy.framework.mq.core.support.EventMessageServiceManager;
import org.gy.framework.mq.model.EventLogContext;
import org.gy.framework.mq.model.EventMessage;
import org.gy.framework.mq.model.EventMessageDispatchResult;
import org.gy.framework.mq.model.IMessageType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;


/**
 * @author gy
 */
@Slf4j
public abstract class AbstractMessageListener implements MessageListener {

    public static final int DEFAULT_RETRY_TIMES = 6;
    public static final long DEFAULT_EXPIRE_TIME = 7200000L;
    public static final String IDEMPOTENT_KEY_PREFIX = "default:mq:idempotentKey";

    @Resource
    private EventMessageDispatchService eventMessageDispatchService;

    @Autowired(required = false)
    private RedissonClient redisson;

    @Resource
    private RocketMqManager rocketMqManager;

    @Resource
    private EventLogService eventLogService;

    protected int getRetryTimes() {
        return DEFAULT_RETRY_TIMES;
    }

    protected long getExpireTime() {
        return DEFAULT_EXPIRE_TIME;
    }

    protected void messageHandler(MessageExt msg) throws Throwable {
        String msgId = msg.getMsgId();
        String msgBody = new String(msg.getBody(), StandardCharsets.UTF_8);

        EventMessage<?> eventMessage = JSON.parseObject(msgBody, EventMessage.class);
        if (eventMessage == null || eventMessage.getEventTypeCode() == null) {
            log.warn("[MessageListener]消息参数错误: msgId={},msgBody={}", msgId, msgBody);
            return;
        }
        log.info("[MessageListener]消息数据: msgId={},msgBody={}", msgId, msgBody);

        EventMessageConsumerService actionService = EventMessageServiceManager.getServiceOptional(eventMessage.getEventTypeCode()).orElse(null);
        if (actionService == null || actionService.getEventTypeCode() == null) {
            log.warn("[MessageListener]消息事件服务无效: event={}", eventMessage.getEventTypeCode());
            return;
        }
        Set<String> supportMessageType = rocketMqManager.getSupportMessageType(this);
        if (!supportMessageType.contains(actionService.getMessageTypeCode())) {
            log.warn("[MessageListener]监听器不支持此消息类型: event={},messageType={}", eventMessage.getEventTypeCode(), actionService.getMessageTypeCode());
            return;
        }

        DistributedLock lock = internalLock(eventMessage);
        boolean lockFlag = Optional.ofNullable(lock).map(DistributedLock::tryLock).orElse(true);
        if (!lockFlag) {
            log.warn("[MessageListener]消息已经处理: msgId={},msgBody={}", msgId, msgBody);
            return;
        }
        internalExecute(eventMessage, msg, lock);
    }

    protected DistributedLock internalLock(EventMessage<?> eventMessage) {
        String idempotentKey = getUniqueKey(eventMessage);
        String code = String.valueOf(eventMessage.getEventTypeCode());
        String redisKey = StringUtils.joinWith(StrUtil.COLON, IDEMPOTENT_KEY_PREFIX, code, idempotentKey);
        return redisson != null ? new RedissonDistributedLock(redisson, redisKey, getExpireTime()) : null;
    }

    protected void internalExecute(EventMessage<?> eventMessage, MessageExt msg, DistributedLock lock) throws Throwable {
        String msgId = msg.getMsgId();
        EventLogContext<EventMessage<?>, Object> ctx = new EventLogContext<>();
        ctx.setRequestId(eventMessage.getRequestId());
        ctx.setRequest(eventMessage);
        EventMessageDispatchResult dispatchResult = eventMessageDispatchService.execute(eventMessage);
        if (dispatchResult.hasException()) {
            //释放幂等key，抛出原异常，方便下次重试处理
            Optional.ofNullable(lock).ifPresent(DistributedLock::unlock);
            //异常重试处理
            internalException(dispatchResult, msg);
            ctx.setEx(dispatchResult.getEx());
        } else {
            ctx.setResponse(dispatchResult.getResult());
        }
        // 保存事件日志（异步）
        EventLogContext.handleEventLog(Collections.singletonList(ctx), eventLogService::batchSaveEventLog);
    }

    protected void internalException(EventMessageDispatchResult dispatchResult, MessageExt msg) throws Throwable {
        Throwable ex = dispatchResult.getEx();
        EventMessageConsumerService eventMessageService = dispatchResult.getService();
        boolean supportRetry = Optional.ofNullable(eventMessageService).map(s -> s.supportRetry(ex)).orElse(false);

        String msgId = msg.getMsgId();
        String msgBody = new String(msg.getBody(), StandardCharsets.UTF_8);
        if (!supportRetry) {
            log.warn("[MessageListener]业务异常暂不支持重试: msgId={},msgBody={}", msgId, msgBody, ex);
            return;
        }
        int retryTimes = getRetryTimes();
        if (retryTimes > 0 && msg.getReconsumeTimes() > retryTimes) {
            // 重试次数超过限制，不再处理，后续可以记录异常表或告警处理
            log.warn("[MessageListener]消息重试次数超过限制: msgId={},msgBody={},retryTimes={}", msgId, msgBody, retryTimes, ex);
        } else {
            throw ex;
        }
    }

    private String getUniqueKey(EventMessage<?> eventMessage) {
        //如果没有传bizKey，则已requestId作为幂等key
        return Optional.ofNullable(eventMessage.getBizKey()).filter(StringUtils::isNotBlank).orElseGet(eventMessage::getRequestId);
    }

}
