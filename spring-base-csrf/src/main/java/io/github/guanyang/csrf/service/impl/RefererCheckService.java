package io.github.guanyang.csrf.service.impl;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import io.github.guanyang.csrf.config.CsrfConfiguration;
import io.github.guanyang.csrf.constant.CheckTypeEnum;
import io.github.guanyang.csrf.service.CheckService;
import io.github.guanyang.util.http.RequestUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

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
