package io.github.guanyang.xss.config.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.github.guanyang.xss.util.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 自定义LocalDateTime反序列化
 *
 * @author guanyang
 */
public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return DateUtils.toLocalDateTime(jsonParser.getText());
    }
}
