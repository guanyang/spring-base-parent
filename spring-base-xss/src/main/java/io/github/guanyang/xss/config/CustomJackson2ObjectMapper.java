package io.github.guanyang.xss.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.guanyang.xss.config.support.CustomLocalDateDeserializer;
import io.github.guanyang.xss.config.support.CustomLocalDateTimeDeserializer;
import io.github.guanyang.xss.config.support.CustomStringJsonDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * 自定义Jackson2ObjectMapper
 *
 * @author guanyang
 */
public class CustomJackson2ObjectMapper implements Jackson2ObjectMapperBuilderCustomizer {

    private final boolean failOnUnknownProperties;

    private final JavaTimeModule javaTimeModule;

    public CustomJackson2ObjectMapper() {
        this.javaTimeModule = defaultJavaTimeModule();
        this.failOnUnknownProperties = false;
    }

    public CustomJackson2ObjectMapper(JavaTimeModule javaTimeModule, boolean failOnUnknownProperties) {
        this.javaTimeModule = javaTimeModule;
        this.failOnUnknownProperties = failOnUnknownProperties;
    }


    @Override
    public void customize(Jackson2ObjectMapperBuilder builder) {
        builder.failOnUnknownProperties(failOnUnknownProperties);
        if (javaTimeModule != null) {
            builder.modules(list -> list.add(javaTimeModule));
        }
    }

    public static JavaTimeModule defaultJavaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        javaTimeModule.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        javaTimeModule.addDeserializer(String.class, new CustomStringJsonDeserializer());
        return javaTimeModule;
    }
}
