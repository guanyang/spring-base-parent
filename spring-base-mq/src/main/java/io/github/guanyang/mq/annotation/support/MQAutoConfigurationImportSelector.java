package io.github.guanyang.mq.annotation.support;

import io.github.guanyang.mq.MQAutoConfiguration;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class MQAutoConfigurationImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{MQAutoConfiguration.class.getName()};
    }
}
