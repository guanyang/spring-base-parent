package io.github.guanyang.log.config;

import io.github.guanyang.log.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Configuration
public class LogWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册日志拦截器
        registry.addInterceptor(new LogInterceptor());
    }

}
