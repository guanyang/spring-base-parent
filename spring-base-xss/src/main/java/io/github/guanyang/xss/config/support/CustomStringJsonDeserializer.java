package io.github.guanyang.xss.config.support;

import com.fasterxml.jackson.databind.DeserializationContext;
import io.github.guanyang.xss.annotation.XssCheck;

import java.io.IOException;

/**
 * 自定义String反序列化
 *
 * @author guanyang
 */
public class CustomStringJsonDeserializer extends BaseJsonDeserializer<String> {

    public CustomStringJsonDeserializer() {
        super();
    }

    public CustomStringJsonDeserializer(boolean xssIgnore, boolean trimIgnore) {
        super(xssIgnore, trimIgnore);
    }

    public CustomStringJsonDeserializer(XssCheck annotation) {
        super(annotation);
    }

    @Override
    protected String convertValue(String text, DeserializationContext ctx) throws IOException {
        return text;
    }

    @Override
    protected BaseJsonDeserializer<String> withAnnotation(XssCheck anno) {
        return new CustomStringJsonDeserializer(anno);
    }
}
