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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public CommonServiceScanAnnotationParser(AnnotationAttributes componentScan, Environment environment, ConfigurableListableBeanFactory beanFactory) {
        this(componentScan, environment, beanFactory, null);
    }

    public CommonServiceScanAnnotationParser(AnnotationAttributes componentScan, Environment environment, ConfigurableListableBeanFactory beanFactory, Set<Class<?>> assignableClasses) {
        this(componentScan, environment, beanFactory, assignableClasses, CommonService.class, COMMON_SERVICE_MAPPER);
    }

    public CommonServiceScanAnnotationParser(AnnotationAttributes componentScan, Environment environment, ConfigurableListableBeanFactory beanFactory, Set<Class<?>> assignableClasses, Class<? extends Annotation> annotationClass, Function<Class<?>, String> beanNameMapper) {
        Assert.isTrue(annotationClass != null || !CollectionUtils.isEmpty(assignableClasses), () -> "annotationClass or assignableClasses is required!");
        this.componentScan = componentScan;
        this.environment = environment;
        this.beanFactory = beanFactory;
        this.annotationClass = annotationClass;
        this.assignableClasses = assignableClasses;
        this.beanNameMapper = beanNameMapper;
    }

    public Map<String, Object> parseAndRegister(Supplier<Set<String>> declaringPackages) {
        String[] basePackages = StringUtils.toStringArray(parse(declaringPackages));
        if (CollectionUtils.isEmpty(assignableClasses)) {
            Set<Class<?>> scanClasses = SpringClassScanner.scanPackage(annotationClass, null, basePackages);
            return SpringClassScanner.register(scanClasses, beanNameMapper, beanFactory);
        }
        Map<String, Object> beanMap = new LinkedHashMap<>();
        assignableClasses.forEach(clazz -> {
            Set<Class<?>> scanClasses = SpringClassScanner.scanPackage(annotationClass, clazz, basePackages);
            Map<String, Object> registerBean = SpringClassScanner.register(scanClasses, beanNameMapper, beanFactory);
            beanMap.putAll(registerBean);
        });
        return beanMap;
    }

    private Set<String> parse(Supplier<Set<String>> declaringPackages) {
        Set<String> basePackages = new LinkedHashSet<>();
        String[] basePackagesArray = componentScan.getStringArray("basePackages");
        for (String pkg : basePackagesArray) {
            String[] tokenized = StringUtils.tokenizeToStringArray(this.environment.resolvePlaceholders(pkg), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            Collections.addAll(basePackages, tokenized);
        }
        for (Class<?> clazz : componentScan.getClassArray("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }
        if (declaringPackages != null) {
            basePackages.addAll(declaringPackages.get());
        }
        return basePackages;
    }
}
