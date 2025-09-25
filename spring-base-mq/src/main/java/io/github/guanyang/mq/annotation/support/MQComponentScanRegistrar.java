package io.github.guanyang.mq.annotation.support;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.core.support.CommonServiceAction;
import io.github.guanyang.core.support.CommonServiceScanAnnotationParser;
import io.github.guanyang.mq.MQAutoConfiguration;
import io.github.guanyang.mq.annotation.EnableMQ;
import io.github.guanyang.mq.model.IEventType;
import io.github.guanyang.mq.model.IMessageType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.Set;

/**
 * @author guanyang
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class MQComponentScanRegistrar implements ImportAware, EnvironmentAware, BeanFactoryAware, InitializingBean, DisposableBean {

    public static final Set<Class<?>> DEFAULT_ASSIGNABLE_CLASSES = Sets.newHashSet(IMessageType.class, IEventType.class);

    private AnnotationAttributes importAttributes;
    private AnnotationMetadata importMetadata;
    private Environment environment;
    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
        Assert.notNull(this.beanFactory, () -> "MQComponentScanRegistrar beanFactory is not ConfigurableListableBeanFactory.");
    }

    @Override
    public void destroy() throws Exception {
        log.info("MQComponentScanRegistrar destroy success.");
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.importMetadata = importMetadata;
        this.importAttributes = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableMQ.class.getName()));
        Assert.notNull(importAttributes, () -> "@EnableMQ is not present on importing class: " + importMetadata.getClassName());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CommonServiceScanAnnotationParser parser = new CommonServiceScanAnnotationParser(this.importAttributes, this.environment, this.beanFactory);
        Map<String, Object> registerBean = parser.parseAndRegister(importMetadata, () -> Sets.newHashSet(ClassUtils.getPackageName(MQAutoConfiguration.class)));
        registerBean.forEach((beanName, bean) -> beanInit(bean));
        log.info("MQComponentScanRegistrar init success, registerBean size: {}", registerBean.size());
    }

    private void beanInit(Object bean) {
        //指定bean需要提前初始化，因为注册动态事件时需要使用
        boolean match = DEFAULT_ASSIGNABLE_CLASSES.stream().anyMatch(clazz -> clazz.isInstance(bean));
        if (match && bean instanceof CommonServiceAction) {
            ((CommonServiceAction) bean).init();
        }
    }
}
