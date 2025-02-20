package org.gy.framework.lock;

import org.gy.framework.lock.annotation.EnableLockAspect;
import org.gy.framework.lock.aop.DistributedLockAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ComponentScan(basePackageClasses = LockCommon.class)
public class LockCommon implements ImportAware {

    protected AnnotationAttributes enableAsync;

    @Bean
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnMissingBean(DistributedLockAspect.class)
    public DistributedLockAspect distributedLockAspect(ApplicationContext applicationContext) {
        //自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
        String redisTemplateName = enableAsync.getString("redisTemplateName");
        StringRedisTemplate stringRedisTemplate = applicationContext.getBean(redisTemplateName, StringRedisTemplate.class);
        return new DistributedLockAspect(stringRedisTemplate);
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableAsync = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableLockAspect.class.getName()));
        if (this.enableAsync == null) {
            throw new IllegalArgumentException("@EnableLockAspect is not present on importing class " + importMetadata.getClassName());
        }
    }

}
