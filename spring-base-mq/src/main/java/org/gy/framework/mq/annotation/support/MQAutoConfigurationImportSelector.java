package org.gy.framework.mq.annotation.support;

import org.gy.framework.mq.MQAutoConfiguration;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class MQAutoConfigurationImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{MQAutoConfiguration.class.getName()};
    }
}
