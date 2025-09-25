package io.github.guanyang.util.http;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

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
