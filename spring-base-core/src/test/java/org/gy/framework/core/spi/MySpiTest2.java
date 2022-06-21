package org.gy.framework.core.spi;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class MySpiTest2 implements SpiTest {

    @Override
    public String type() {
        return "test2";
    }

    @Override
    public String hello() {
        return "test2 hello";
    }
}
