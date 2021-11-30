package org.gy.framework.csrf.service.impl;

import java.net.URI;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.csrf.config.CsrfConfiguration;
import org.gy.framework.csrf.constant.CheckTypeEnum;
import org.gy.framework.csrf.service.CheckService;
import org.gy.framework.util.http.RequestUtils;
import org.springframework.stereotype.Service;

/**
 * @author gy
 */
@Service
@Slf4j
public class RefererCheckService implements CheckService {

    @Resource
    private CsrfConfiguration csrfConfiguration;

    @Override
    public boolean check(HttpServletRequest request) {
        return isRefererValid(RequestUtils.referer(request));
    }

    @Override
    public void afterCheck(HttpServletResponse response) {
    }

    @Override
    public CheckTypeEnum checkType() {
        return CheckTypeEnum.REFERER;
    }

    private boolean isRefererValid(String referer) {
        if (StringUtils.isBlank(referer)) {
            log.warn("check is_referer_valid, referer is blank.");
            return false;
        }

        try {
            String refererHost = new URI(referer).getHost();
            List<String> configRefererHosts = csrfConfiguration.refererHosts();

            log.debug("check is_referer_valid, referer:{}, referer_host:{}, config_referer_hosts:{}.",
                refererHost, refererHost, configRefererHosts);

            return configRefererHosts.contains(refererHost);
        } catch (Exception e) {
            log.error("error when checking referer, referer:{}.", referer, e);
        }

        return false;
    }

}
