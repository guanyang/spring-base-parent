package io.github.guanyang.util.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.core.exception.CommonErrorCode;
import io.github.guanyang.core.exception.CommonException;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * 功能描述：参数验证工具类
 *
 * @author bailishouyue
 * @version 1.0.0
 */
@Slf4j
public class ValidationUtil {

    public static final int LENGTH_LIMIT = 100;
    public static final String STR = ";";
    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    private ValidationUtil() {
    }

    public static <T> void validate(T t, Class<?>... groups) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(t, groups);
        if (CollectionUtils.isEmpty(constraintViolations)) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (ConstraintViolation<T> constraintViolation : constraintViolations) {
            builder.append(constraintViolation.getMessage()).append(STR);
        }
        if (builder.length() > LENGTH_LIMIT) {
            builder.setLength(LENGTH_LIMIT);
        }
        throw new CommonException(CommonErrorCode.PARAM_ERROR.getError(), builder.toString());
    }

}
