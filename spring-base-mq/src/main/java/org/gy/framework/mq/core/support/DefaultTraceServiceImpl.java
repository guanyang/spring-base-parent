package org.gy.framework.mq.core.support;

import org.gy.framework.core.trace.TraceUtils;
import org.gy.framework.mq.core.TraceService;
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
