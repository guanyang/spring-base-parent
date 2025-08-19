package org.gy.framework.core.support;

import org.gy.framework.core.annotation.CommonService;
import org.gy.framework.core.util.StringUtil;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

public class CommonServiceScanAnnotationParser {

    public static final Function<Class<?>, String> COMMON_SERVICE_MAPPER = clazz -> {
        CommonService commonService = AnnotationUtils.findAnnotation(clazz, CommonService.class);
        return Optional.ofNullable(commonService).map(CommonService::value).filter(StringUtil::hasText).orElseGet(clazz::getSimpleName);
    };

    private final AnnotationAttributes componentScan;

    private final Environment environment;

    private final ConfigurableListableBeanFactory beanFactory;

    private final Class<? extends Annotation> annotationClass;

    private final Set<Class<?>> assignableClasses;

    private final Function<Class<?>, String> beanNameMapper;

    public CommonServiceScanAnnotationParser(AnnotationAttributes componentScan, Environment environment, ConfigurableListableBeanFactory beanFactory, Set<Class<?>> assignableClasses) {
        this(componentScan, environment, beanFactory, assignableClasses, CommonService.class, COMMON_SERVICE_MAPPER);
    }

    public CommonServiceScanAnnotationParser(AnnotationAttributes componentScan, Environment environment, ConfigurableListableBeanFactory beanFactory, Set<Class<?>> assignableClasses, Class<? extends Annotation> annotationClass, Function<Class<?>, String> beanNameMapper) {
        Assert.notNull(annotationClass, "annotationClass must not be null");
        Assert.notNull(assignableClasses, "assignableClasses must not be null");
        this.componentScan = componentScan;
        this.environment = environment;
        this.beanFactory = beanFactory;
        this.annotationClass = annotationClass;
        this.assignableClasses = assignableClasses;
        this.beanNameMapper = beanNameMapper;
    }

    public Map<String, Object> parseAndRegister(Class<?> declaringClass) {
        Map<String, Object> result = new HashMap<>();

        String[] basePackages = StringUtils.toStringArray(parse(declaringClass));
        assignableClasses.forEach(clazz -> {
            Set<Class<?>> scanClasses = SpringClassScanner.scanPackage(annotationClass, clazz, basePackages);
            Map<String, Object> registerBean = SpringClassScanner.register(scanClasses, beanNameMapper, beanFactory);
            result.putAll(registerBean);
        });
        return result;
    }

    private Set<String> parse(Class<?> declaringClass) {
        Set<String> basePackages = new LinkedHashSet<>();
        String[] basePackagesArray = componentScan.getStringArray("basePackages");
        for (String pkg : basePackagesArray) {
            String[] tokenized = StringUtils.tokenizeToStringArray(this.environment.resolvePlaceholders(pkg), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            Collections.addAll(basePackages, tokenized);
        }
        for (Class<?> clazz : componentScan.getClassArray("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }
        if (declaringClass != null) {
            basePackages.add(ClassUtils.getPackageName(declaringClass));
        }
        return basePackages;
    }
}
