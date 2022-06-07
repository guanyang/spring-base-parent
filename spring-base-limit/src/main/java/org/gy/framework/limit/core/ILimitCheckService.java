package org.gy.framework.limit.core;

import org.gy.framework.limit.core.support.LimitCheckContext;

/**
 * 频率限制检查接口定义
 *
 * @author gy
 * @version 1.0.0
 */
public interface ILimitCheckService {


    /**
     * 频率限制类型
     */
    String type();

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
