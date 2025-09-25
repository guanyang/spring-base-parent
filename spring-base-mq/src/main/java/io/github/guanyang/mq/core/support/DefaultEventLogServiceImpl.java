package io.github.guanyang.mq.core.support;

import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.mq.core.EventLogService;
import io.github.guanyang.mq.model.EventLogContext;
import io.github.guanyang.mq.model.EventMessage;

import java.util.List;

@Slf4j
public class DefaultEventLogServiceImpl implements EventLogService {
    @Override
    public <R> void batchSaveEventLog(List<EventLogContext<EventMessage<?>, R>> ctxList) {
        log.info("[EventLogService]ctxList:{}", ctxList);
    }
}
