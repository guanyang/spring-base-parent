package org.gy.framework.csrf.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.gy.framework.csrf.constant.CheckTypeEnum;

/**
 * @author gy
 */
public interface CheckService {

    boolean check(HttpServletRequest request);

    void afterCheck(HttpServletResponse response);

    CheckTypeEnum checkType();

}
