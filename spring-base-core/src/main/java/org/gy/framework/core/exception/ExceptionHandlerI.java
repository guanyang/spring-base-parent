package org.gy.framework.core.exception;

import org.gy.framework.core.dto.BaseResponse;
import org.gy.framework.core.dto.DTO;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public interface ExceptionHandlerI {

    void handleException(DTO dto, BaseResponse response, Exception exception);

}
