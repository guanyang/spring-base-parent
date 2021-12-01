package org.gy.framework.log;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author gy
 */
@Configuration
@ComponentScan(basePackageClasses = LogCommon.class)
public class LogCommon {

}
