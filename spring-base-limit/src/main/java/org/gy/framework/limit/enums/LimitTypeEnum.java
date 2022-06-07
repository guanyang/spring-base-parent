package org.gy.framework.limit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 频率限制类型定义
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum LimitTypeEnum {

    REDIS("redis", "redis频率限制");

    private final String code;

    private final String desc;

}
