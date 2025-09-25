package io.github.guanyang.core.exception;

import io.github.guanyang.core.dto.BaseResponse;
import io.github.guanyang.core.dto.DTO;
import io.github.guanyang.core.spi.SpiIdentity;

/**
 * 异常处理接口定义
 *
 * @author gy
 * @version 1.0.0
 */
public interface ExceptionHandlerI extends SpiIdentity {

    void handleException(DTO dto, BaseResponse response, Exception exception);

}
