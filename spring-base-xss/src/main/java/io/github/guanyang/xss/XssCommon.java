package io.github.guanyang.xss;

import io.github.guanyang.xss.aspect.XssCheckAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gy
 */
@Configuration
public class XssCommon {

    @Bean
    @ConditionalOnMissingBean(XssCheckAspect.class)
    public XssCheckAspect xssCheckAspect() {
        return new XssCheckAspect();
    }

}
