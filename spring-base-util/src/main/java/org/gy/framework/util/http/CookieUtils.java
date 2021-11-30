package org.gy.framework.util.http;

import java.util.stream.Stream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author gy
 */
public class CookieUtils {

    public static String getValue(HttpServletRequest request, String name) {
        if (ArrayUtils.isEmpty(request.getCookies())) {
            return StringUtils.EMPTY;
        }

        return Stream.of(request.getCookies())
            .filter(c -> StringUtils.equals(c.getName(), name))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(StringUtils.EMPTY);
    }

    public static void setCookie(HttpServletResponse response, String name, String value, int expire) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(expire);

        response.addCookie(cookie);
    }

}
