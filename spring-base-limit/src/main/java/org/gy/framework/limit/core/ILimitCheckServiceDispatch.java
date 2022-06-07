package org.gy.framework.limit.core;

/**
 * 频率限制检查分发定义
 *
 * @author gy
 * @version 1.0.0
 */
public interface ILimitCheckServiceDispatch {

    ILimitCheckService findService(String type);

}
