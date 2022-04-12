package org.gy.framework.util.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gy.framework.core.exception.Assert;
import org.gy.framework.core.support.IStdEnum;

/**
 * 删除枚举定义
 *
 * @author gy
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum DeletedEnum implements IStdEnum<Integer> {

    //正常枚举
    NO(0, "正常"),

    //删除枚举
    YES(1, "删除");

    private final Integer code;

    private final String desc;

    public static DeletedEnum codeOf(Integer code) {
        DeletedEnum deletedEnum = DeletedEnum.codeOf(code, null);
        Assert.notNull(deletedEnum, "unknown DeletedEnum code:" + code);
        return deletedEnum;
    }

    public static DeletedEnum codeOf(Integer code, DeletedEnum defaultEnum) {
        return IStdEnum.codeOf(DeletedEnum.class, code, defaultEnum);
    }

}
