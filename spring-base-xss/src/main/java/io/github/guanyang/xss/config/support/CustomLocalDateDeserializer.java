package io.github.guanyang.xss.config.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.github.guanyang.xss.util.DateUtils;

import java.io.IOException;
import java.time.LocalDate;

/**
 * 自定义LocalDate反序列化
 *
 * @author guanyang
 */
public class CustomLocalDateDeserializer extends JsonDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return DateUtils.toLocalDate(jsonParser.getText());
    }
}
