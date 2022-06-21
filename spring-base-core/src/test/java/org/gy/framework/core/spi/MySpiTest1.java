package org.gy.framework.core.spi;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class MySpiTest1 implements SpiTest {

    @Override
    public String type() {
        return "test1";
    }

    @Override
    public String hello() {
        return "test1 hello";
    }
}
