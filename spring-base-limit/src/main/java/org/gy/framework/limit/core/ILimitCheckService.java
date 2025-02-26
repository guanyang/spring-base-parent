package org.gy.framework.limit.core;

import org.gy.framework.core.spi.SpiIdentity;
import org.gy.framework.limit.model.LimitCheckContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 频率限制检查接口定义
 *
 * @author gy
 * @version 1.0.0
 */
public interface ILimitCheckService extends SpiIdentity {

    String KEY_FORMAT = "{%s}:%s";

    Long SUCCESS = 1L;

    default List<String> getKeys(String hashTag, String... keyNames) {
        // use `{}` around keys to use Redis Key hash tags
        // this allows for using redis cluster
        return Stream.of(keyNames).map(keyName -> String.format(KEY_FORMAT, hashTag, keyName)).collect(Collectors.toList());
    }


    /**
     * 检查频率是否超过阈值，true是，false否
     *
     * @param context 频率限制检查上下文
     * @return 是否超过阈值，true是，false否
     * @author gy
     * @version 1.0.0
     */
    boolean check(LimitCheckContext context);

}
