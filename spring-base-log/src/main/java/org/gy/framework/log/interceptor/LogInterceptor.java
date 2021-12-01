package org.gy.framework.log.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.trace.TraceUtils;
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
        TraceUtils.setTraceIdIfAbsent();
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) throws Exception {
        log.debug("postHandle running ...");
        TraceUtils.removeTraceId();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
        log.debug("afterCompletion running ...");
    }
}
