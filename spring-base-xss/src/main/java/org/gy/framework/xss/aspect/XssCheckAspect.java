package org.gy.framework.xss.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.xss.exception.XssException;
import org.gy.framework.xss.util.ClassCheckUtils;
import org.gy.framework.xss.util.XssTool;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 功能描述：请求对象的String字段自动进行trim、checkXss
 *
 * @author gy
 */
@Slf4j
@Aspect
@Component
public class XssCheckAspect {

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public void pointcutName() {
    }


    @Before("pointcutName()")
    public void executeAround(JoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Annotation[][] methodAnnotations = method.getParameterAnnotations();
        Object[] obj = pjp.getArgs();
        if (obj == null) {
            return;
        }
        for (int i = 0; i < obj.length; i++) {
            if (this.checkAnnotation(methodAnnotations[i])) {
                this.preHandle(obj[i]);
            }
        }
    }

    private void preHandle(Object requestDataObj) throws XssException {
        //检查参数类型，true继续执行，false终止执行
        boolean checkType = checkType(requestDataObj);
        if (!checkType) {
            return;
        }
        Class clazz = requestDataObj.getClass();
        List<Field> fields = this.getFields(clazz);

        for (Field field : fields) {
            Object fieldValue = null;
            field.setAccessible(true);

            try {
                String fieldName = field.getName();
                fieldValue = field.get(requestDataObj);
                if (Objects.isNull(fieldValue)) {
                    continue;
                }
                // 处理字符串字段
                if (String.class.isAssignableFrom(field.getType())) {
                    String strValue = StringUtils.trim((String) fieldValue);
                    field.set(requestDataObj, strValue);
                    if (XssTool.matchXSS(strValue)) {
                        StringBuilder buf = new StringBuilder("参数：").append(fieldName).append("不符合XSS校验");
                        throw new XssException(buf.toString());
                    }
                }
                // 处理集合字段
                if (Collection.class.isAssignableFrom(field.getType())) {
                    Collection listValue = (Collection) fieldValue;
                    for (Object item : listValue) {
                        this.preHandle(item);
                    }
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    Map mapValue = (Map) fieldValue;
                    mapValue.forEach((k, v) -> {
                        if (v != null) {
                            this.preHandle(v);
                        }
                    });
                }

            } catch (IllegalAccessException e) {
                log.error("没有访问权限|{}", e);
                continue;
            }
        }

    }

    private List<Field> getFields(Class clazz) {
        List<Field> fieldList = new ArrayList<>();
        Class tempClass = clazz;
        //当父类为null的时候说明到达了最上层的父类(Object类).
        while (tempClass != null) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            //得到父类,然后赋给自己
            tempClass = tempClass.getSuperclass();
        }
        Collections.reverse(fieldList);
        return fieldList;
    }

    private boolean checkAnnotation(Annotation[] annotations) {
        if (annotations == null) {
            return false;
        }
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Valid.class || annotation.annotationType() == Validated.class) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查参数类型，true继续执行，false终止执行
     */
    private static boolean checkType(Object requestObj) {
        if (requestObj == null) {
            return false;
        }
        Class clazz = requestObj.getClass();
        //不是指定的类型，则继续执行
        boolean basicType = ClassCheckUtils.checkBasicType(clazz);
        if (!basicType) {
            return true;
        }
        //如果是字符串类型，检查是否包含恶意数据
        if (requestObj instanceof String && XssTool.matchXSS((String) requestObj)) {
            throw new XssException("参数不符合XSS校验");
        }
        return false;
    }

}
