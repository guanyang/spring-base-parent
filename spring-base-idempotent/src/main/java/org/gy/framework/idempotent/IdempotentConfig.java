package org.gy.framework.idempotent;

import org.gy.framework.idempotent.annotation.EnableIdempotent;
import org.gy.framework.idempotent.aop.IdempotentAspect;
import org.gy.framework.idempotent.core.IdempotentKeyResolver;
import org.gy.framework.idempotent.core.support.DefaultIdempotentKeyResolver;
import org.gy.framework.idempotent.core.support.ExpressionIdempotentKeyResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@Configuration
@ComponentScan(basePackageClasses = IdempotentConfig.class)
public class IdempotentConfig implements ImportAware {

    protected AnnotationAttributes enableAsync;

    @Bean
    @ConditionalOnMissingBean(IdempotentAspect.class)
    public IdempotentAspect idempotentAspect(ApplicationContext applicationContext, List<IdempotentKeyResolver> keyResolvers) {
        //自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
        String redisTemplateName = enableAsync.getString("redisTemplateName");
        StringRedisTemplate stringRedisTemplate = applicationContext.getBean(redisTemplateName, StringRedisTemplate.class);
        return new IdempotentAspect(stringRedisTemplate, keyResolvers);
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableAsync = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableIdempotent.class.getName()));
        if (this.enableAsync == null) {
            throw new IllegalArgumentException("@EnableIdempotent is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
