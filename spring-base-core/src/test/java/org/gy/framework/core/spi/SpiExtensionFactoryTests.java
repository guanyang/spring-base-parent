package org.gy.framework.core.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class SpiExtensionFactoryTests {

    @Test
    public void getExtensionTest1() {
        SpiTest spiTest = SpiExtensionFactory.getExtension("test1", SpiTest.class);
        assertEquals("test1 hello", spiTest.hello());

        spiTest = SpiExtensionFactory.getExtension("test2", SpiTest.class);
        assertEquals("test2 hello", spiTest.hello());
    }

    @Test
    public void getExtensionTest2() {
        SpiCustom spiTest = SpiExtensionFactory.getExtension("MySpiCustom1", SpiCustom.class);
        assertEquals("MySpiCustom1", spiTest.run());

        spiTest = SpiExtensionFactory.getExtension("MySpiCustom2", SpiCustom.class);
        assertEquals(Integer.MAX_VALUE, spiTest.run());
    }

}
