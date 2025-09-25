package org.gy.framework.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.ArrayUtils;
import org.gy.framework.core.support.IStdEnum;
import org.gy.framework.core.support.IStdEnum.StdEnumFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Object> {

    private Set<Object> values = new HashSet<>();

    @Override
    public void initialize(ListValue initValue) {
        //加载枚举数据
        wrapValue(initValue.enumValue(), val -> {
            StdEnumFactory.findFromCache(val, IStdEnum::getCode).forEach((k, v) -> values.add(k));
        });
        //加载String数据
        wrapValue(initValue.stringValue(), values::add);
        //加载int数据
        wrapValue(initValue.intValue(), values::add);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return values.contains(value);
    }

    private static <T> void wrapValue(T[] values, Consumer<? super T> action) {
        if (ArrayUtils.isNotEmpty(values)) {
            Arrays.stream(values).forEach(action);
        }
    }

    private static void wrapValue(int[] intValues, IntConsumer consumer) {
        if (ArrayUtils.isNotEmpty(intValues)) {
            Arrays.stream(intValues).forEach(consumer);
        }
    }

}
