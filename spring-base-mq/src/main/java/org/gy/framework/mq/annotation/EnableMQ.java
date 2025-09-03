package org.gy.framework.mq.annotation;

import org.gy.framework.mq.MqConfig;
import org.gy.framework.mq.config.KafkaConfiguration;
import org.gy.framework.mq.config.RocketMQConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MqConfig.class, KafkaConfiguration.class, RocketMQConfiguration.class})
public @interface EnableMQ {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

}
