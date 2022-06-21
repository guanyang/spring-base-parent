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
    public void getCachedInstancesTest1() {
        Map<String, Object> cachedInstances = SpiExtensionFactory.getCachedInstances(SpiTest.class);
        assertEquals(2, cachedInstances.size());
    }

    @Test
    public void getExtensionTest1() {
        SpiTest spiTest = SpiExtensionFactory.getExtension("test1", SpiTest.class);
        assertEquals("test1 hello", spiTest.hello());
    }

    @Test
    public void getCachedInstancesTest2() {
        Map<String, Object> cachedInstances = SpiExtensionFactory.getCachedInstances(SpiCustom.class);
        assertEquals(2, cachedInstances.size());
    }

    @Test
    public void getExtensionTest2() {
        SpiCustom spiTest = SpiExtensionFactory.getExtension("MySpiCustom1", SpiCustom.class);
        assertEquals("MySpiCustom1", spiTest.run());

        spiTest = SpiExtensionFactory.getExtension("MySpiCustom2", SpiCustom.class);
        assertEquals(Integer.MAX_VALUE, spiTest.run());
    }

}
