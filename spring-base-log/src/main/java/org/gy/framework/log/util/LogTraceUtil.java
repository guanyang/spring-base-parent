package org.gy.framework.log.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.core.trace.TraceUtils;
import org.gy.framework.core.util.JsonUtils;
import org.gy.framework.log.model.TraceRequest;
import org.gy.framework.util.http.ClientIpUtils;
import org.gy.framework.util.net.NetUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * 功能描述：日志记录工具类
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class LogTraceUtil {

    private static final ThreadLocal<Map<String, Object>> logDetailLocal = new ThreadLocal<>();

    private static final int MAX_STR_LENGTH = 10000;
    /**
     * 服务端IP
     */
    private static final String HOST_IP = NetUtils.getLocalHost();
    private static final String LOG_VERSION = "v1";

    private LogTraceUtil() {
    }

    public static <T, U, R> R execute(T req, Function<T, TraceRequest<U>> preFunction, Action<T, R> doAction)
        throws Throwable {
        R result = null;
        try {
            //前置处理
            TraceRequest<U> traceRequest = preFunction.apply(req);
            preTrace(traceRequest);
            //执行业务逻辑
            result = doAction.proceed(req);
            // 后置处理
            postTrace(result, traceRequest);
        } catch (Throwable e) {
            log.error("LogTraceUtil proceed exception.", e);
            postTraceWithException(e);
            throw e;
        } finally {
            clearTrace();
        }
        return result;
    }

    private static <T> void preTrace(TraceRequest<T> request) {
        try {
            Map<String, Object> detail = getThreadLocalLogDetail();
            if (request != null) {
                if (request.getExecuteClazz() != null) {
                    detail.put("className", request.getExecuteClazz().getName());
                }
                detail.put("methodName", request.getExecuteMethodName());
                detail.put("desc", request.getDesc());

                boolean requestBodyTrace = request.isRequestBodyTrace();
                if (requestBodyTrace) {
                    String json = objectToJson(request.getRequestObj());
                    detail.put("requestBody", json);
                }
            }
            detail.put("invokeStartTime", System.currentTimeMillis());
        } catch (Throwable e) {
            log.warn("LogTraceUtil preTrace Exception.", e);
        }
    }

    public static interface Action<T, R> {

        R proceed(T t) throws Throwable;
    }

    private static <U, R> void postTrace(R responseObj, TraceRequest<U> traceRequest) {
        try {
            Map<String, Object> detail = getThreadLocalLogDetail();
            wrapCostTime(detail);

            boolean responseBodyTrace = traceRequest.isResponseBodyTrace();
            if (responseBodyTrace) {
                String json = objectToJson(responseObj);
                detail.put("responseBody", json);
            }

            logMessage(detail);
        } catch (Throwable e) {
            log.warn("LogTraceUtil postTrace Exception.", e);
        }
    }

    private static void postTraceWithException(Throwable e) {
        try {
            Map<String, Object> detail = getThreadLocalLogDetail();
            wrapCostTime(detail);

            detail.put("hasException", true);
            detail.put("exceptionMsg", e.toString());

            logMessage(detail);
        } catch (Throwable exp) {
            log.warn("LogTraceUtil postTrace Exception.", exp);
        }
    }

    private static void clearTrace() {
        try {
            logDetailLocal.remove();
        } catch (Throwable e) {
            log.warn("LogTraceUtil clear Exception.", e);
        }
    }

    private static void logMessage(Map<String, Object> detail) {
        detail.put("logEndTime", System.currentTimeMillis());
        log.info(JsonUtils.toJson(detail));
    }

    private static void wrapCostTime(Map<String, Object> detail) {
        long currentTimeMillis = System.currentTimeMillis();
        detail.put("invokeEndTime", currentTimeMillis);
        Long time = (Long) detail.get("invokeStartTime");
        if (time != null) {
            detail.put("invokeCostTime", currentTimeMillis - time);
        }
    }

    private static Map<String, Object> getThreadLocalLogDetail() {
        Map<String, Object> logDetail = logDetailLocal.get();
        if (logDetail == null) {
            logDetail = initLogDetail();
            logDetailLocal.set(logDetail);
        }
        return logDetail;
    }

    private static Map<String, Object> initLogDetail() {
        Map<String, Object> logDetail = new HashMap<>();
        logDetail.put("logStartTime", System.currentTimeMillis());
        logDetail.put("requestId", TraceUtils.computeIfAbsent());
        logDetail.put("serverIp", HOST_IP);
        logDetail.put("clientIp", getClientIp());
        logDetail.put("version", LOG_VERSION);
        return logDetail;
    }

    private static String errorMessage(String message) {
        return "{\"errorMessage\":\"" + message + "\"}";
    }

    private static <R> String objectToJson(R obj) {
        if (obj != null) {
            String result;
            try {
                if (obj instanceof ModelAndView) {
                    result = JsonUtils.toJson(((ModelAndView) obj).getModelMap());
                } else if (obj instanceof String) {
                    result = (String) obj;
                } else if (obj instanceof HttpServletRequest || obj instanceof HttpServletResponse) {
                    //HttpServletRequest、HttpServletResponse 序列化报错，暂不处理
                    result = null;
                } else {
                    result = JsonUtils.toJson(obj);
                }
                result = abbreviate(result);
            } catch (Exception e) {
                log.warn("LogTraceUtil serialize Exception.", e);
                result = errorMessage("serialize Exception:" + e.toString());
            }
            return result;
        }
        return null;
    }

    private static String abbreviate(String str) {
        // 避免消息超长，影响磁盘IO
        return StringUtils.abbreviate(str, MAX_STR_LENGTH);
    }

    /**
     * 功能描述: 获取客户端IP
     *
     * @author gy
     */
    private static String getClientIp() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return null;
            }
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            // 获取客户端ip地址
            return ClientIpUtils.clientIp(request);
        } catch (Exception e) {
            log.warn("LogTraceUtil getClientIp exception", e);
        }
        return null;
    }

}
