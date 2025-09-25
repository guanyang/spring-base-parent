package io.github.guanyang.mq.config.support;

import io.github.guanyang.core.util.CollectionUtils;
import io.github.guanyang.mq.config.MqManager;
import io.github.guanyang.mq.config.MqManagerAction;
import io.github.guanyang.mq.core.EventMessageHandler;
import io.github.guanyang.mq.model.MqType;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DefaultMqManager implements MqManager {

    private final Map<MqType, MqManagerAction<?, ?>> actionMap;
    private final Map<MqType, EventMessageHandler> messageHandlerMap;

    public DefaultMqManager(List<MqManagerAction<?, ?>> mqManagerActions, List<EventMessageHandler> messageHandlers) {
        this.actionMap = CollectionUtils.convertMap(mqManagerActions, MqManagerAction::getMqType, Function.identity());
        this.messageHandlerMap = CollectionUtils.convertMap(messageHandlers, EventMessageHandler::getMqType, Function.identity());
    }

    @Override
    public <P, C> MqManagerAction<P, C> getManagerAction(MqType mqType) {
        Assert.notNull(mqType, () -> "MqManager mqType is null");
        MqManagerAction<P, C> action = (MqManagerAction<P, C>) actionMap.get(mqType);
        Assert.notNull(action, () -> "MqManager action is not exist: " + mqType);
        return action;
    }

    @Override
    public EventMessageHandler getMessageHandler(MqType mqType) {
        Assert.notNull(mqType, () -> "MqManager mqType is null");
        EventMessageHandler messageHandler = messageHandlerMap.get(mqType);
        Assert.notNull(messageHandler, () -> "MqManager handle is not exist: " + mqType);
        return messageHandler;
    }

}
