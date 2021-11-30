package org.gy.framework.csrf.config;

import javax.annotation.Resource;
import org.gy.framework.csrf.interceptor.CsrfInterceptor;
import org.gy.framework.csrf.service.impl.CheckContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author gy
 */
@Configuration
public class WebAppConfig implements WebMvcConfigurer {

    @Resource
    private CheckContext checkContext;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry
            .addInterceptor(new CsrfInterceptor(this.checkContext));
        interceptorRegistration.excludePathPatterns("/asserts/**", "/error/**", "/index.html", "/", "/user/login");
        interceptorRegistration.addPathPatterns("/**");
    }


}
