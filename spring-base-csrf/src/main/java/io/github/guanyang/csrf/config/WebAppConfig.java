package io.github.guanyang.csrf.config;

import jakarta.annotation.Resource;
import io.github.guanyang.csrf.interceptor.CsrfInterceptor;
import io.github.guanyang.csrf.service.impl.CheckContext;
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
