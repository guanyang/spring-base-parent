package org.gy.framework.core.exception;

import org.gy.framework.core.dto.BaseResponse;
import org.gy.framework.core.dto.DTO;
import org.gy.framework.core.spi.SpiIdentity;

/**
 * 异常处理接口定义
 *
 * @author gy
 * @version 1.0.0
 */
public interface ExceptionHandlerI extends SpiIdentity {

    void handleException(DTO dto, BaseResponse response, Exception exception);

}
