package org.gy.framework.limit.core.support;

import cn.hutool.extra.servlet.ServletUtil;
import org.aspectj.lang.JoinPoint;
import org.gy.framework.limit.annotation.LimitCheck;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 客户端IP级别限流key解析器
 *
 * @author gy
 */
@Component
public class ClientIpLimitKeyResolver extends AbstractLimitKeyResolver {
    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, LimitCheck annotation) {
        return paramKeyBuilder(joinPoint, k -> k.append(getClientIP()));
    }

    public static String getClientIP() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return ServletUtil.getClientIP(request);
    }

    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
}
