package org.gy.framework.csrf.service.impl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.gy.framework.csrf.config.CsrfConfiguration;
import org.gy.framework.csrf.service.CsrfTokenService;
import org.gy.framework.csrf.util.TokenUtils;
import org.gy.framework.util.http.CookieUtils;
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
