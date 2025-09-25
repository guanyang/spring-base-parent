package io.github.guanyang.util.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.github.guanyang.core.support.IStdEnum;

import java.util.Objects;

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
        DeletedEnum item = DeletedEnum.codeOf(code, null);
        return Objects.requireNonNull(item, () -> "unknown DeletedEnum error:" + code);
    }

    public static DeletedEnum codeOf(Integer code, DeletedEnum defaultEnum) {
        return IStdEnum.codeOf(DeletedEnum.class, code, defaultEnum);
    }

}
