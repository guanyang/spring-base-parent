package org.gy.framework.core.exception.support;

import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.dto.BaseResponse;
import org.gy.framework.core.dto.DTO;
import org.gy.framework.core.exception.CommonErrorCode;
import org.gy.framework.core.exception.CommonException;
import org.gy.framework.core.exception.ExceptionHandlerI;

/**
 * 功能描述：DefaultExceptionHandler
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class DefaultExceptionHandler implements ExceptionHandlerI {

    private static final String DEFAULT_MSG_FORMAT = "Process [{0}] failed, errorCode: {1}, errorMsg: {2}";

    private static DefaultExceptionHandler singleton = new DefaultExceptionHandler();

    public static ExceptionHandlerI getInstance() {
        return DefaultExceptionHandler.singleton;
    }

    @Override
    public void handleException(DTO dto, BaseResponse response, Exception exception) {
        buildResponse(response, exception);
        printLog(dto, response, exception);
    }


    private void printLog(DTO dto, BaseResponse response, Exception exception) {
        if (exception instanceof CommonException) {
            //biz exception is expected, only warn it
            log.warn(buildErrorMsg(dto, response));
        } else {
            //sys exception should be monitored, and pay attention to it
            log.error(buildErrorMsg(dto, response), exception);
        }
    }

    private String buildErrorMsg(DTO dto, BaseResponse response) {
        return MessageFormat.format(DEFAULT_MSG_FORMAT, dto, String.valueOf(response.getError()), response.getMsg());
    }

    private void buildResponse(BaseResponse response, Exception exception) {
        if (exception instanceof CommonException) {
            CommonException e = (CommonException) exception;
            response.setError(e.getError());
            response.setMsg(e.getMsg());
        } else {
            response.setError(CommonErrorCode.SYS_SERVICE_ERROR.getError());
            response.setMsg(exception.getMessage());
        }
    }

}
