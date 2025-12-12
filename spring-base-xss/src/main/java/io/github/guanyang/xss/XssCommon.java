package io.github.guanyang.xss;

import io.github.guanyang.xss.config.CustomAnnotationFormatterFactory;
import io.github.guanyang.xss.config.CustomJackson2ObjectMapper;
import io.github.guanyang.xss.config.CustomStringFormatter;
import io.github.guanyang.xss.util.DateUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author gy
 */
@Configuration
public class XssCommon implements WebMvcConfigurer {

    public static final String CUSTOM_JACKSON_OBJECT_MAPPER = "customJackson2ObjectMapper";

    @Bean
    @ConditionalOnMissingBean(name = CUSTOM_JACKSON_OBJECT_MAPPER)
    public Jackson2ObjectMapperBuilderCustomizer customJackson2ObjectMapper() {
        return new CustomJackson2ObjectMapper();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 1. 未带 @XssCheck 的（默认 XSS 检查）
        registry.addFormatterForFieldType(String.class, new CustomStringFormatter());
        // 2. 带 @XssCheck 的（跳过检查）
        registry.addFormatterForFieldAnnotation(new CustomAnnotationFormatterFactory());

        // 注册 LocalDate 类型的解析器，不适用于json传参
        registry.addConverter(String.class, LocalDate.class, DateUtils::toLocalDate);
        // 注册 LocalDateTime 类型的解析器，不适用于json传参
        registry.addConverter(String.class, LocalDateTime.class, DateUtils::toLocalDateTime);
    }

}
