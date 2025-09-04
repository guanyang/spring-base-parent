package org.gy.framework.mq.annotation;

import org.gy.framework.mq.annotation.support.MQAutoConfigurationImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MQAutoConfigurationImportSelector.class)
public @interface EnableMQ {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

}
