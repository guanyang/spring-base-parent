package org.gy.framework.limit;

import java.util.Optional;
import org.gy.framework.limit.annotation.EnableLimitCheck;
import org.gy.framework.limit.aop.LimitCheckAspect;
import org.gy.framework.limit.core.ILimitCheckServiceDispatch;
import org.gy.framework.limit.core.support.DefaultLimitCheckServiceDispatch;
import org.gy.framework.limit.core.support.RedisLimitCheckService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 频率检查配置入口
 *
 * @author gy
 * @version 1.0.0
 */
@Configuration
@ComponentScan(basePackageClasses = LimitCheckConfig.class)
public class LimitCheckConfig implements ImportAware {

    protected AnnotationAttributes enableAsync;

    @Bean
    @ConditionalOnClass(StringRedisTemplate.class)
    public LimitCheckAspect limitCheckAspect(ApplicationContext context) {
        ILimitCheckServiceDispatch dispatch = new DefaultLimitCheckServiceDispatch();
        //自定义redisTemplate名称，方便切面注入指定bean，解决应用中存在多个redisTemplate的问题
        String name = enableAsync.getString("redisTemplateName");
        Optional.ofNullable(name).map(n -> context.getBean(n, StringRedisTemplate.class))
            .map(RedisLimitCheckService::new).ifPresent(DefaultLimitCheckServiceDispatch::addLimitCheckIfAbsent);
        return new LimitCheckAspect(dispatch);
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableAsync = AnnotationAttributes.fromMap(
            importMetadata.getAnnotationAttributes(EnableLimitCheck.class.getName()));
        if (this.enableAsync == null) {
            throw new IllegalArgumentException(
                "@EnableLimitCheck is not present on importing class " + importMetadata.getClassName());
        }
    }
}
