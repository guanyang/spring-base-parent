package io.github.guanyang.csrf.util;

import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import io.github.guanyang.csrf.constant.Constants;

/**
 * @author gy
 */
public class TokenUtils {

    private static final String SR = "-";
    private static final int LEN_KEY = 32;

    public static String generate() {
        String key = generateKey();
        return key + sign(key);
    }

    public static boolean isValid(String token) {
        String key = StringUtils.substring(token, 0, LEN_KEY);
        String sign = StringUtils.substring(token, LEN_KEY);

        return StringUtils.equals(sign(key), sign);
    }

    private static String generateKey() {
        return UUID.randomUUID().toString().replaceAll(SR, StringUtils.EMPTY);
    }

    private static String sign(String key) {
        return DigestUtils.md5Hex(key + Constants.SALT_TOKEN_SIGN);
    }

}
