package org.gy.framework.util.response;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.util.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);

    private static final String CALLBACK = "callback";

    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    private static final String CONTENT_TYPE_JSONP = "application/x-javascript;charset=UTF-8";

    private ResponseUtil() {
    }

    /**
     * 功能描述: json输出，采用fastjson将对象转换成json <br>
     * <li>入参为封装对象</li><br>
     * <li>有callback，则jsonp输出，没有则json输出</li>
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param object 待输出对象
     */
    public static void ajaxJson(HttpServletRequest request, HttpServletResponse response, Object object) {
        ajaxJson(request, response, JsonUtils.toJson(object));
    }

    /**
     * 功能描述: json输出<br>
     * <li>入参为json字符串</li><br>
     * <li>有callback，则jsonp输出，没有则json输出</li>
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param jsonString json字符串
     */
    public static void ajaxJson(HttpServletRequest request, HttpServletResponse response, String jsonString) {
        String callback = request.getParameter(CALLBACK);
        if (StringUtils.isNotBlank(callback)) {
            StringBuilder result = new StringBuilder();
            result.append(callback).append("(").append(jsonString).append(")");
            ajax(response, result.toString(), CONTENT_TYPE_JSONP);
        } else {
            ajaxJson(response, jsonString);
        }
    }

    /**
     * 功能描述: json输出
     *
     * @param response 响应对象
     * @param jsonString json字符串
     */
    private static void ajaxJson(HttpServletResponse response, String jsonString) {
        ajax(response, jsonString, CONTENT_TYPE_JSON);
    }

    /**
     * 功能描述: ajax输出
     *
     * @param response 响应对象
     * @param content 输出内容
     * @param type 输出格式
     */
    private static void ajax(HttpServletResponse response, String content, String contentType) {
        try {
            response.setContentType(contentType);
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
            response.setDateHeader("Expires", 0L);

            response.getWriter().write(content);
            response.getWriter().flush();
        } catch (IOException e) {
            logger.error("ajax IOException:content={},contentType={}.", content, contentType, e);
        }
    }

}
