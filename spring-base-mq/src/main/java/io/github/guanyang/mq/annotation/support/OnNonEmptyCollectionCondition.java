package io.github.guanyang.mq.annotation.support;

import org.apache.commons.lang3.StringUtils;
import io.github.guanyang.mq.annotation.ConditionalOnNonEmptyCollection;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnNonEmptyCollectionCondition implements Condition {

    public static final String PREFIX_NAME = "prefix";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(ConditionalOnNonEmptyCollection.class.getName()));
        String prefix = attributes.getString(PREFIX_NAME);
        if (StringUtils.isBlank(prefix)) {
            return false;
        }
        Binder binder = Binder.get(context.getEnvironment());
        BindResult<List<Object>> list = binder.bind(prefix, Bindable.listOf(Object.class));
        if (list.isBound()) {
            return true;
        }
        BindResult<Map<String, Object>> map = binder.bind(prefix, Bindable.mapOf(String.class, Object.class));
        if (map.isBound()) {
            return true;
        }
        BindResult<Set<Object>> set = binder.bind(prefix, Bindable.setOf(Object.class));
        return set.isBound();
    }
}
