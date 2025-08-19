package org.gy.framework.mq.core.support;

import lombok.extern.slf4j.Slf4j;
import org.gy.framework.mq.core.EventLogService;
import org.gy.framework.mq.model.EventLogContext;
import org.gy.framework.mq.model.EventMessage;

import java.util.List;

@Slf4j
public class DefaultEventLogServiceImpl implements EventLogService {
    @Override
    public <R> void batchSaveEventLog(List<EventLogContext<EventMessage<?>, R>> ctxList) {
        log.info("[EventLogService]ctxList:{}", ctxList);
    }
}
