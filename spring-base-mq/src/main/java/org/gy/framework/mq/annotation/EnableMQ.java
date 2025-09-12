package org.gy.framework.mq.annotation;

import org.gy.framework.mq.MQAutoConfiguration.ExcludeAutoConfiguration;
import org.gy.framework.mq.annotation.support.MQAutoConfigurationImportSelector;
import org.gy.framework.mq.annotation.support.MQComponentScanRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MQComponentScanRegistrar.class, MQAutoConfigurationImportSelector.class, ExcludeAutoConfiguration.class})
public @interface EnableMQ {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

}
