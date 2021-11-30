package org.gy.framework.csrf.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.gy.framework.csrf.constant.CheckTypeEnum;

/**
 * @author gy
 */
public interface CheckService {

    boolean check(HttpServletRequest request);

    void afterCheck(HttpServletResponse response);

    CheckTypeEnum checkType();

}
