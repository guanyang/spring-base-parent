package io.github.guanyang.core.spi;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class MySpiCustom1 implements SpiCustom<String> {

    @Override
    public String type() {
        return "MySpiCustom1";
    }

    @Override
    public String run() {
        return "MySpiCustom1";
    }
}
