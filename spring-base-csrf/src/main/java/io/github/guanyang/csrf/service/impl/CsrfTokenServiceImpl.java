package io.github.guanyang.csrf.service.impl;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.guanyang.csrf.config.CsrfConfiguration;
import io.github.guanyang.csrf.service.CsrfTokenService;
import io.github.guanyang.csrf.util.TokenUtils;
import io.github.guanyang.util.http.CookieUtils;
import org.springframework.stereotype.Service;

/**
 * @author gy
 */
@Service
public class CsrfTokenServiceImpl implements CsrfTokenService {

    @Resource
    private CsrfConfiguration csrfConfiguration;

    @Override
    public String getToken(HttpServletRequest request) {
        return CookieUtils.getValue(request, csrfConfiguration.tokenName());
    }

    @Override
    public void setToken(HttpServletResponse response) {
        CookieUtils
            .setCookie(response, csrfConfiguration.tokenName(), TokenUtils.generate(), csrfConfiguration.tokenMaxAge());
    }

    @Override
    public boolean isValid(String token) {
        return TokenUtils.isValid(token);
    }

}
