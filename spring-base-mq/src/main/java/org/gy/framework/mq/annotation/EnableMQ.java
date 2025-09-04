package org.gy.framework.mq.annotation;

import org.gy.framework.mq.MQAutoConfiguration.ExcludeAutoConfiguration;
import org.gy.framework.mq.annotation.support.MQAutoConfigurationImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ExcludeAutoConfiguration.class, MQAutoConfigurationImportSelector.class})
public @interface EnableMQ {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

}
