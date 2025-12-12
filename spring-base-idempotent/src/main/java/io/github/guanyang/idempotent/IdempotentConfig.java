package io.github.guanyang.idempotent;

import io.github.guanyang.idempotent.annotation.EnableIdempotent;
import io.github.guanyang.idempotent.aop.IdempotentAspect;
import io.github.guanyang.idempotent.core.IdempotentKeyResolver;
import io.github.guanyang.idempotent.core.IdempotentService;
import io.github.guanyang.idempotent.core.support.DefaultIdempotentServiceImpl;
import io.github.guanyang.lock.aop.support.CustomCachedExpressionEvaluator;
import org.redisson.api.RedissonClient;
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
    public IdempotentService idempotentService(List<IdempotentKeyResolver> keyResolvers, CustomCachedExpressionEvaluator evaluator) {
//        //自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
//        String redisTemplateName = enableAsync.getString("redisTemplateName");
//        StringRedisTemplate stringRedisTemplate = context.getBean(redisTemplateName, StringRedisTemplate.class);
//        return new DefaultIdempotentServiceImpl(keyResolvers, stringRedisTemplate, evaluator);

        //自定义redissonClient名称，方便切面注入指定bean，解决应用中存在多个redissonClient的问题
        String redissonClientName = enableAsync.getString("redissonClientName");
        RedissonClient client = context.getBean(redissonClientName, RedissonClient.class);
        return new DefaultIdempotentServiceImpl(keyResolvers, client, evaluator);
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
