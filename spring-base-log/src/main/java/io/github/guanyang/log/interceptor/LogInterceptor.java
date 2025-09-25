package io.github.guanyang.log.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import io.github.guanyang.core.trace.TraceUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 功能描述：日志拦截器生成traceid,供日志打印和返回对象使用
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        log.debug("preHandle running ...");
        String traceid = request.getHeader(TraceUtils.HTTP_HEADER_TRACE_ID);
        if (StringUtils.isBlank(traceid)) {
            TraceUtils.setTraceIdIfAbsent();
        } else {
            TraceUtils.setTraceId(traceid);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) throws Exception {
        log.debug("postHandle running ...");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
        log.debug("afterCompletion running ...");
        TraceUtils.removeTraceId();
    }
}
