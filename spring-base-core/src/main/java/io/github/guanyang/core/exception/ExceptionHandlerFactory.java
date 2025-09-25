package io.github.guanyang.core.exception;

import java.util.Optional;
import io.github.guanyang.core.exception.support.DefaultExceptionHandler;
import io.github.guanyang.core.spi.SpiExtensionFactory;

/**
 * 异常处理工厂
 *
 * @author gy
 * @version 1.0.0
 */
public class ExceptionHandlerFactory {

    public static ExceptionHandlerI getHandler(String type) {
        return Optional.ofNullable(type).map(t -> SpiExtensionFactory.getExtension(t, ExceptionHandlerI.class))
            .orElseGet(DefaultExceptionHandler::getInstance);
    }

    public static ExceptionHandlerI getHandler() {
        return DefaultExceptionHandler.getInstance();
    }

}
