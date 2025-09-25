package io.github.guanyang.lock.utils;

import java.util.UUID;

/**
 * 功能描述：ID工具类
 *
 * @author gy
 * @version 1.0.0
 */
public class IdUtils {

    private IdUtils() {
    }

    public static String defaultRequestId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
