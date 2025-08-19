package org.gy.framework.mq.config.support;

import cn.hutool.extra.spring.SpringUtil;
import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.common.message.MessageExt;
import org.gy.framework.mq.core.TraceService;

/**
 * @author gy
 */
public class TraceConsumeMessageHook implements ConsumeMessageHook {

    private static final String DEFAULT_HOOK_NAME = "TraceConsumeMessageHook";

    private final TraceService traceService;

    public TraceConsumeMessageHook() {
        this.traceService = SpringUtil.getBean(TraceService.class);
    }

    @Override
    public String hookName() {
        return DEFAULT_HOOK_NAME;
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        if (context == null || context.getMsgList() == null || context.getMsgList().isEmpty()) {
            return;
        }
        //默认获取第一个消息的trace信息即可
        MessageExt messageExt = context.getMsgList().get(0);
        if (messageExt != null) {
            String traceKey = traceService.getTraceKey();
            String traceId = messageExt.getUserProperty(traceKey);
            traceService.setTrace(traceId);
        }

    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
        traceService.clearTrace();
    }
}
