package org.gy.framework.util.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {ListValueConstraintValidator.class})
public @interface ListValue {

    String message() default "参数类型错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 枚举类型值定义，枚举需要实现IStdEnum接口
     */
    Class<? extends Enum>[] enumValue() default {};

    /**
     * string类型值定义
     */
    String[] stringValue() default {};

    /**
     * int类型值定义
     */
    int[] intValue() default {};

}
