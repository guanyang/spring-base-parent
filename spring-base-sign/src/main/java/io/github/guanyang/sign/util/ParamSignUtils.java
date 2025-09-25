package io.github.guanyang.sign.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import io.github.guanyang.sign.annotation.SignParam;
import io.github.guanyang.sign.dto.SignedReq;
import io.github.guanyang.sign.exception.SignInvalidException;

/**
 * @author gy
 */
@Slf4j
public class ParamSignUtils {

    private static final String PARAM_NAME_KEY = "appKey";
    private static final String SEPRATOR_PARAM = "&";
    private static final String SEPRATOR_KV = "=";

    public static <T extends SignedReq> void checkSign(T t, String key) {
        String calSign = sign(t, key);
        if (!StringUtils.equalsIgnoreCase(calSign, t.getSign())) {
            throw new SignInvalidException("sign invalid");
        }
    }

    public static <T> String sign(T t, String key) {
        List<String> kvList = FieldUtils.getFieldsListWithAnnotation(t.getClass(), SignParam.class).stream()
            .sorted(Comparator.comparing(ParamSignUtils::getSignParamName))
            .map(f -> joinKV(ParamSignUtils.getSignParamName(f), getFieldValue(f, t))).collect(Collectors.toList());
        return md5(joinKVStrs(joinKVStrs(kvList), joinKV(PARAM_NAME_KEY, key)));
    }

    private static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }

    private static String joinKV(String key, String value) {
        return StringUtils.join(key, SEPRATOR_KV, value);
    }

    private static String joinKVStrs(String... params) {
        return joinKVStrs(Arrays.asList(params));
    }

    private static String joinKVStrs(List<String> params) {
        return StringUtils.join(params, SEPRATOR_PARAM);
    }

    private static <T> String getFieldValue(Field field, T t) {
        try {
            return String.valueOf(FieldUtils.readField(field, t, true));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getSignParamName(Field field) {
        String name = field.getAnnotation(SignParam.class).name();
        return StringUtils.defaultIfBlank(name, field.getName());
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
