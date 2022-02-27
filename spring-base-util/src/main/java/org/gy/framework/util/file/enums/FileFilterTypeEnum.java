package org.gy.framework.util.file.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 功能描述：过滤器类型定义
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum FileFilterTypeEnum {

    //过滤器类型定义
    IMAGE_TYPE("图片类型过滤");

    private final String desc;
}
