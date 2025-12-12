package io.github.guanyang.xss.config;

import io.github.guanyang.xss.annotation.XssCheck;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import java.util.Set;

/**
 * 自定义注解格式化工厂
 *
 * @author guanyang
 */
public class CustomAnnotationFormatterFactory implements AnnotationFormatterFactory<XssCheck> {
    @Override
    public Set<Class<?>> getFieldTypes() {
        return Set.of(String.class);
    }

    @Override
    public Printer<?> getPrinter(XssCheck annotation, Class<?> fieldType) {
        return new CustomStringFormatter(annotation);
    }

    @Override
    public Parser<?> getParser(XssCheck annotation, Class<?> fieldType) {
        return new CustomStringFormatter(annotation);
    }
}
