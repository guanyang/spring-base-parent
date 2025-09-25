package io.github.guanyang.csrf.service.impl;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.csrf.config.CsrfConfiguration;
import io.github.guanyang.csrf.constant.CheckTypeEnum;
import io.github.guanyang.csrf.service.CheckService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gy
 */
@Service
@Slf4j
public class CheckContext {

    @Resource
    private List<CheckService> checkServiceList;
    @Resource
    private CsrfConfiguration csrfConfiguration;

    public boolean check(HttpServletRequest request, HttpServletResponse response) {
        boolean res = csrfConfiguration.checkTypes().stream().map(c -> doCheck(c, request))
            .filter(cr -> !cr).findFirst().orElse(true);

        if (res) {
            csrfConfiguration.checkTypes().forEach(c -> doAfterCheck(c, response));
        }

        return res;
    }

    private boolean doCheck(CheckTypeEnum checkType, HttpServletRequest request) {
        try {
            return checkServiceList.stream().filter(c -> checkType == c.checkType())
                .map(c -> c.check(request)).findFirst().orElse(false);
        } catch (Exception e) {
            log.error("error when checking, check_type:{}.", checkType, e);
        }

        return false;
    }

    private void doAfterCheck(CheckTypeEnum checkType, HttpServletResponse response) {
        try {
            checkServiceList.stream().filter(c -> checkType == c.checkType())
                .forEach(c -> c.afterCheck(response));
        } catch (Exception e) {
            log.error("error when do_after_check, check_type:{}.", checkType, e);
        }
    }

}
