package io.github.guanyang.csrf.service.impl;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import io.github.guanyang.csrf.config.CsrfConfiguration;
import io.github.guanyang.csrf.constant.CheckTypeEnum;
import io.github.guanyang.csrf.service.CheckService;
import io.github.guanyang.csrf.service.CsrfTokenService;
import io.github.guanyang.util.http.RequestUtils;
import org.springframework.stereotype.Service;

/**
 * @author gy
 */
@Service
@Slf4j
public class TokenCheckService implements CheckService {

    @Resource
    private CsrfConfiguration csrfConfiguration;
    @Resource
    private CsrfTokenService csrfTokenService;

    @Override
    public boolean check(HttpServletRequest request) {
        String reqToken = RequestUtils.parameterValue(request, csrfConfiguration.paramName());
        String cookieToken = csrfTokenService.getToken(request);

        log.debug("check token, req_token:{}, cookie_token:{}.", reqToken, cookieToken);

        if (StringUtils.isAnyBlank(reqToken, cookieToken)) {
            return false;
        }

        if (!csrfTokenService.isValid(cookieToken)) {
            log.warn("invalid cookie_token, cookie_token:{}.", cookieToken);
            return false;
        }

        return StringUtils.equals(reqToken, cookieToken);
    }

    @Override
    public void afterCheck(HttpServletResponse response) {
        csrfTokenService.setToken(response);
    }

    @Override
    public CheckTypeEnum checkType() {
        return CheckTypeEnum.TOKEN;
    }

}
