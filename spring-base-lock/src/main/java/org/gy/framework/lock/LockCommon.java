package org.gy.framework.lock;

import org.gy.framework.lock.annotation.EnableLockAspect;
import org.gy.framework.lock.aop.DistributedLockAspect;
import org.gy.framework.lock.aop.support.CustomCachedExpressionEvaluator;
import org.gy.framework.lock.core.ILockService;
import org.gy.framework.lock.core.LockKeyResolver;
import org.gy.framework.lock.core.support.DefaultLockServiceImpl;
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
@ComponentScan(basePackageClasses = LockCommon.class)
public class LockCommon implements ImportAware, ApplicationContextAware {

    protected AnnotationAttributes enableAsync;

    protected ApplicationContext context;

    @Bean
    @ConditionalOnMissingBean(DistributedLockAspect.class)
    public DistributedLockAspect distributedLockAspect(ILockService lockService) {
        return new DistributedLockAspect(lockService);
    }

    @Bean
    @ConditionalOnMissingBean(ILockService.class)
    public ILockService lockService(List<LockKeyResolver> keyResolvers) {
        //自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
        String redisTemplateName = enableAsync.getString("redisTemplateName");
        StringRedisTemplate stringRedisTemplate = context.getBean(redisTemplateName, StringRedisTemplate.class);
        return new DefaultLockServiceImpl(keyResolvers, stringRedisTemplate);
    }

    @Bean("lockExpressionEvaluator")
    @ConditionalOnMissingBean(CustomCachedExpressionEvaluator.class)
    public CustomCachedExpressionEvaluator customCachedExpressionEvaluator(ConfigurableBeanFactory beanFactory) {
        return new CustomCachedExpressionEvaluator(beanFactory);
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableAsync = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableLockAspect.class.getName()));
        if (this.enableAsync == null) {
            throw new IllegalArgumentException("@EnableLockAspect is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
