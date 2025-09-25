package io.github.guanyang.csrf.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.guanyang.csrf.constant.CheckTypeEnum;

/**
 * @author gy
 */
public interface CheckService {

    boolean check(HttpServletRequest request);

    void afterCheck(HttpServletResponse response);

    CheckTypeEnum checkType();

}
