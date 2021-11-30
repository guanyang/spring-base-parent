package org.gy.framework.csrf.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

/**
 * @author gy
 */
@Service
public interface CsrfTokenService {

    String getToken(HttpServletRequest request);

    void setToken(HttpServletResponse response);

    boolean isValid(String token);

}
