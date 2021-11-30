package org.gy.framework.csrf.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.csrf.service.CsrfTokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gy
 */
@RestController
@ConditionalOnProperty(name = "csrf.tokenUrl")
public class CsrfTokenController {

    @Resource
    private CsrfTokenService csrfTokenService;

    @GetMapping("${csrf.tokenUrl}")
    public String token(HttpServletResponse response) {
        csrfTokenService.setToken(response);
        return StringUtils.EMPTY;
    }

}
