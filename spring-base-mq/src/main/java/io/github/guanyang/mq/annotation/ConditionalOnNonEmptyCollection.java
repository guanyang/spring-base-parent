package io.github.guanyang.mq.annotation;

import io.github.guanyang.mq.annotation.support.OnNonEmptyCollectionCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnNonEmptyCollectionCondition.class)
public @interface ConditionalOnNonEmptyCollection {

    /**
     * 配置前缀
     */
    String prefix();
}
