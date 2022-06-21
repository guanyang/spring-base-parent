package org.gy.framework.core.spi;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public interface SpiCustom<T> extends SpiIdentity {

    T run();

}
