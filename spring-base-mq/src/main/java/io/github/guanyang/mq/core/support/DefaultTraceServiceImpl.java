package io.github.guanyang.mq.core.support;

import io.github.guanyang.core.trace.TraceUtils;
import io.github.guanyang.mq.core.TraceService;
import org.springframework.stereotype.Service;

@Service
public class DefaultTraceServiceImpl implements TraceService {

    @Override
    public String getTraceKey() {
        return TraceUtils.getLogTraceKey();
    }

    @Override
    public String getTraceId() {
        return TraceUtils.getTraceId();
    }

    @Override
    public void setTrace(String traceId) {
        TraceUtils.setTraceId(traceId);
    }

    @Override
    public void clearTrace() {
        TraceUtils.removeTraceId();
    }
}
