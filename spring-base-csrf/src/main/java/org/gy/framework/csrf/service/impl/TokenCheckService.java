package org.gy.framework.csrf.service.impl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.csrf.config.CsrfConfiguration;
import org.gy.framework.csrf.constant.CheckTypeEnum;
import org.gy.framework.csrf.service.CheckService;
import org.gy.framework.csrf.service.CsrfTokenService;
import org.gy.framework.util.http.RequestUtils;
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
