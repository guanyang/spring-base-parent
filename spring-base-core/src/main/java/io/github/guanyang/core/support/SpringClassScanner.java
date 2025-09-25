package io.github.guanyang.core.support;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.core.util.StringUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SpringClassScanner {

    /**
     * 注册指定的类到Spring容器
     *
     * @param classes        指定类
     * @param beanNameMapper bean名称转换器
     * @param beanFactory    Spring容器
     */
    public static Map<String, Object> register(Set<Class<?>> classes, Function<Class<?>, String> beanNameMapper, ConfigurableListableBeanFactory beanFactory) {
        if (CollectionUtils.isEmpty(classes) || beanFactory == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> beanMap = new LinkedHashMap<>();
        classes.forEach(clazz -> {
            registerBean(clazz, beanNameMapper, beanFactory, beanMap);
        });
        return beanMap;
    }

    /**
     * 扫描指定包下指定继承的类
     *
     * @param assignableClass
     * @param basePackages
     * @return
     */
    public static Set<Class<?>> scanPackageByAssignableClass(Class<?> assignableClass, String... basePackages) {
        Assert.notNull(basePackages, () -> "basePackages is required!");
        Assert.notNull(assignableClass, () -> "assignableClass is required!");
        return scanPackage(null, assignableClass, basePackages);
    }

    /**
     * 扫描指定包下指定注解的类
     *
     * @param annotationClass
     * @param basePackages
     * @return
     */
    public static Set<Class<?>> scanPackageByAnnotationClass(Class<? extends Annotation> annotationClass, String... basePackages) {
        Assert.notNull(basePackages, () -> "basePackages is required!");
        Assert.notNull(annotationClass, () -> "annotationClass is required!");
        return scanPackage(annotationClass, null, basePackages);
    }

    /**
     * 扫描指定包下指定继承的类和指定注解的类
     *
     * @param annotationClass
     * @param assignableClass
     * @param basePackages
     * @return
     */
    public static Set<Class<?>> scanPackage(Class<? extends Annotation> annotationClass, Class<?> assignableClass, String... basePackages) {
        Assert.notNull(basePackages, () -> "basePackages is required!");
        Assert.isTrue(annotationClass != null || assignableClass != null, () -> "annotationClass or assignableClass is required!");
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        AndTypeFilter andTypeFilter = new AndTypeFilter();
        if (annotationClass != null) {
            andTypeFilter.setLeft(new AnnotationTypeFilter(annotationClass));
        }
        if (assignableClass != null) {
            andTypeFilter.setRight(new AssignableTypeFilter(assignableClass));
        }
        scanner.addIncludeFilter(andTypeFilter);
        return findClasses(scanner, basePackages);
    }

    private static Set<Class<?>> findClasses(ClassPathScanningCandidateComponentProvider scanner, String... basePackages) {
        if (ObjectUtils.isEmpty(basePackages)) {
            return Collections.emptySet();
        }
        return Stream.of(basePackages).map(base -> findClasses(scanner, base)).flatMap(Set::stream).collect(Collectors.toSet());
    }

    private static Set<Class<?>> findClasses(ClassPathScanningCandidateComponentProvider scanner, String basePackage) {
        Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
        Set<Class<?>> classes = new HashSet<>();
        for (BeanDefinition bd : candidates) {
            String beanClassName = bd.getBeanClassName();
            try {
                classes.add(Class.forName(beanClassName));
            } catch (Exception ignore) {
                log.warn("[SpringClassScanner]load class error: basePackage={}, beanClassName={} ", basePackage, beanClassName, ignore);
            }
        }
        return classes;
    }

    @SneakyThrows
    private static void registerBean(Class<?> clazz, Function<Class<?>, String> beanNameMapper, ConfigurableListableBeanFactory beanFactory, Map<String, Object> beanMap) {
        if (clazz.isEnum()) {
            registerBeanForEnum(clazz, beanNameMapper, beanFactory, beanMap);
        } else {
            registerBeanForNormal(clazz, beanNameMapper, beanFactory, beanMap);
        }
    }

    private static void registerBeanForEnum(Class<?> clazz, Function<Class<?>, String> beanNameMapper, ConfigurableListableBeanFactory beanFactory, Map<String, Object> beanMap) {
        String simpleName = Optional.ofNullable(beanNameMapper).map(mapper -> mapper.apply(clazz)).orElseGet(clazz::getSimpleName);
        Object[] constants = clazz.getEnumConstants();
        for (Object constant : constants) {
            String beanName = uniqueKey(simpleName, ((Enum<?>) constant).name());
            if (!beanFactory.containsBean(beanName)) {
                beanFactory.registerSingleton(beanName, constant);
                beanMap.put(beanName, beanFactory.getBean(beanName));
            }
        }
    }

    private static void registerBeanForNormal(Class<?> clazz, Function<Class<?>, String> beanNameMapper, ConfigurableListableBeanFactory beanFactory, Map<String, Object> beanMap) throws Exception {
        String beanName = Optional.ofNullable(beanNameMapper).map(mapper -> mapper.apply(clazz)).orElseGet(clazz::getSimpleName);
        if (!beanFactory.containsBean(beanName)) {
            Object instance = clazz.newInstance();
            beanFactory.autowireBean(instance);
            beanFactory.initializeBean(instance, beanName);
            beanFactory.registerSingleton(beanName, instance);
            beanMap.put(beanName, beanFactory.getBean(beanName));
        }
    }

    private static String uniqueKey(CharSequence... elements) {
        return String.join(StringUtil.UNDERLINE, elements);
    }

    @Data
    public static class AndTypeFilter implements TypeFilter {

        private TypeFilter left;
        private TypeFilter right;

        public AndTypeFilter() {
        }

        public AndTypeFilter(TypeFilter left, TypeFilter right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
            return (left == null || left.match(metadataReader, metadataReaderFactory)) && (right == null || right.match(metadataReader, metadataReaderFactory));
        }
    }
}
