package org.gy.framework.core.exception;

import java.util.Optional;
import org.gy.framework.core.exception.support.DefaultExceptionHandler;
import org.gy.framework.core.spi.SpiExtensionFactory;

/**
 * 异常处理工厂
 *
 * @author gy
 * @version 1.0.0
 */
public class ExceptionHandlerFactory {

    public static ExceptionHandlerI getHandler(String type) {
        return Optional.ofNullable(type).map(t -> SpiExtensionFactory.getExtension(type, ExceptionHandlerI.class))
            .orElseGet(DefaultExceptionHandler::getInstance);
    }

    public static ExceptionHandlerI getHandler() {
        return DefaultExceptionHandler.getInstance();
    }

}
