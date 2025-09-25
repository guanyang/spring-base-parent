package io.github.guanyang.mq.core;

import io.github.guanyang.mq.model.EventLogContext;
import io.github.guanyang.mq.model.EventMessage;

import java.util.List;

public interface EventLogService {

    /**
     * 批量保存事件日志
     */
    <R> void batchSaveEventLog(List<EventLogContext<EventMessage<?>, R>> ctxList);
}
