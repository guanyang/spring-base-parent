package io.github.guanyang.xss.config.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import io.github.guanyang.xss.annotation.XssCheck;
import io.github.guanyang.xss.exception.XssException;
import io.github.guanyang.xss.util.XssTool;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guanyang
 */
public abstract class BaseJsonDeserializer<T> extends JsonDeserializer<T> implements ContextualDeserializer {

    private static final Map<Class<?>, Map<XssCheck, BaseJsonDeserializer<?>>> CACHE = new ConcurrentHashMap<>();

    protected final boolean xssIgnore;
    protected final boolean trimIgnore;

    public BaseJsonDeserializer() {
        this(false, false);
    }

    public BaseJsonDeserializer(boolean xssIgnore, boolean trimIgnore) {
        this.xssIgnore = xssIgnore;
        this.trimIgnore = trimIgnore;
    }

    public BaseJsonDeserializer(XssCheck annotation) {
        this.xssIgnore = !annotation.check();
        this.trimIgnore = !annotation.trim();
    }

    protected abstract T convertValue(String text, DeserializationContext ctx) throws IOException;

    protected abstract BaseJsonDeserializer<T> withAnnotation(XssCheck anno);

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String text = p.getText();
        if (!xssIgnore && XssTool.matchXss(text)) {
            throw new XssException("Parameter safety check error", text);
        }
        String processed = trimIgnore ? text : StringUtils.trim(text);
        return convertValue(processed, ctx);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        XssCheck xssCheck = Optional.ofNullable(beanProperty).map(b -> b.getAnnotation(XssCheck.class)).orElse(null);
        if (xssCheck != null) {
            Class<?> rawClass = beanProperty.getType().getRawClass();
            return createInstance(rawClass, xssCheck);
        }
        return this;
    }

    protected BaseJsonDeserializer<?> createInstance(Class<?> rawClass, XssCheck xssCheck) {
        Map<XssCheck, BaseJsonDeserializer<?>> deserializerMap = CACHE.computeIfAbsent(rawClass, k -> new ConcurrentHashMap<>());
        return deserializerMap.computeIfAbsent(xssCheck, this::withAnnotation);
    }
}
