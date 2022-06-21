package org.gy.framework.core.spi;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class MySpiCustom2 implements SpiCustom<Integer> {

    @Override
    public String type() {
        return "MySpiCustom2";
    }

    @Override
    public Integer run() {
        return Integer.MAX_VALUE;
    }
}
