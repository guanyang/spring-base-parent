package org.gy.framework.idempotent;

import org.gy.framework.idempotent.annotation.EnableIdempotent;
import org.gy.framework.idempotent.aop.IdempotentAspect;
import org.gy.framework.idempotent.core.IdempotentKeyResolver;
import org.gy.framework.idempotent.core.IdempotentService;
import org.gy.framework.idempotent.core.support.DefaultIdempotentServiceImpl;
import org.gy.framework.lock.aop.support.CustomCachedExpressionEvaluator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class IdempotentConfig implements ImportAware, ApplicationContextAware {

    protected AnnotationAttributes enableAsync;

    protected ApplicationContext context;

    @Bean
    @ConditionalOnMissingBean(IdempotentAspect.class)
    public IdempotentAspect idempotentAspect(IdempotentService idempotentService) {
        return new IdempotentAspect(idempotentService);
    }

    @Bean
    @ConditionalOnMissingBean(IdempotentService.class)
    public IdempotentService idempotentService(List<IdempotentKeyResolver> keyResolvers) {
        //自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
        String redisTemplateName = enableAsync.getString("redisTemplateName");
        StringRedisTemplate stringRedisTemplate = context.getBean(redisTemplateName, StringRedisTemplate.class);
        return new DefaultIdempotentServiceImpl(keyResolvers, stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(CustomCachedExpressionEvaluator.class)
    public CustomCachedExpressionEvaluator idempotentExpressionEvaluator(ConfigurableBeanFactory beanFactory) {
        return new CustomCachedExpressionEvaluator(beanFactory);
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableAsync = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableIdempotent.class.getName()));
        if (this.enableAsync == null) {
            throw new IllegalArgumentException("@EnableIdempotent is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
