package org.gy.framework.mq.core;

import org.gy.framework.mq.model.EventLogContext;
import org.gy.framework.mq.model.EventMessage;

import java.util.List;

public interface EventLogService {

    /**
     * 批量保存事件日志
     */
    <R> void batchSaveEventLog(List<EventLogContext<EventMessage<?>, R>> ctxList);
}
