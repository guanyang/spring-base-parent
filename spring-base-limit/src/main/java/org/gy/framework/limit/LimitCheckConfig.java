package org.gy.framework.limit;

import org.gy.framework.limit.annotation.EnableLimitCheck;
import org.gy.framework.limit.aop.LimitCheckAspect;
import org.gy.framework.limit.aop.support.CustomCachedExpressionEvaluator;
import org.gy.framework.limit.core.ILimitCheckServiceDispatch;
import org.gy.framework.limit.core.LimitKeyResolver;
import org.gy.framework.limit.core.support.DefaultLimitCheckServiceDispatch;
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

/**
 * 频率检查配置入口
 *
 * @author gy
 * @version 1.0.0
 */
@Configuration
@ComponentScan(basePackageClasses = LimitCheckConfig.class)
public class LimitCheckConfig implements ImportAware, ApplicationContextAware {

    protected AnnotationAttributes enableAsync;

    protected ApplicationContext context;

    @Bean
    @ConditionalOnMissingBean(LimitCheckAspect.class)
    public LimitCheckAspect limitCheckAspect(ILimitCheckServiceDispatch dispatch) {
        return new LimitCheckAspect(dispatch);
    }

    @Bean
    @ConditionalOnMissingBean(ILimitCheckServiceDispatch.class)
    public ILimitCheckServiceDispatch limitCheckServiceDispatch(List<LimitKeyResolver> keyResolvers, CustomCachedExpressionEvaluator evaluator) {
        //自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
        String redisTemplateName = enableAsync.getString("redisTemplateName");
        StringRedisTemplate stringRedisTemplate = context.getBean(redisTemplateName, StringRedisTemplate.class);
        return new DefaultLimitCheckServiceDispatch(keyResolvers, evaluator, stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(CustomCachedExpressionEvaluator.class)
    public CustomCachedExpressionEvaluator limitExpressionEvaluator(ConfigurableBeanFactory beanFactory) {
        return new CustomCachedExpressionEvaluator(beanFactory);
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableAsync = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableLimitCheck.class.getName()));
        if (this.enableAsync == null) {
            throw new IllegalArgumentException("@EnableLimitCheck is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
