package org.gy.framework.mq.annotation.support;

import com.google.common.collect.Sets;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.Set;

import static org.gy.framework.mq.MQAutoConfiguration.KAFKA;
import static org.gy.framework.mq.MQAutoConfiguration.ROCKETMQ;

@Deprecated
public class MQAutoConfigurationImportFilter implements AutoConfigurationImportFilter {
    //需要排除的全限定类名，类存在则排除，不存在则忽略
    public static final Set<String> EXCLUDES = Sets.newHashSet(ROCKETMQ, KAFKA);

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] result = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            result[i] = !EXCLUDES.contains(autoConfigurationClasses[i]);
        }
        return result;
    }
}
