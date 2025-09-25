package org.gy.framework.csrf.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.gy.framework.csrf.annotation.CsrfCheck;
import org.gy.framework.csrf.exception.CsrfException;
import org.gy.framework.csrf.service.impl.CheckContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * @author gy
 */
public class CsrfInterceptor implements HandlerInterceptor {

    private final CheckContext checkContext;

    public CsrfInterceptor(CheckContext checkContext) {
        this.checkContext = checkContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod hm = null;
        if (handler instanceof HandlerMethod) {
            hm = (HandlerMethod) handler;
        } else {
            return HandlerInterceptor.super.preHandle(request, response, handler);
        }

        Method method = hm.getMethod();
        if (!method.isAnnotationPresent(CsrfCheck.class)) {
            return HandlerInterceptor.super.preHandle(request, response, handler);
        }

        if (!checkContext.check(request, response)) {
            throw new CsrfException("invalid req");
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

}
