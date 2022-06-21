package org.gy.framework.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.gy.framework.core.spi.SpiExtensionFactory;
import org.gy.framework.core.spi.SpiIdentity;
import org.junit.jupiter.api.Test;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class SpiExtensionFactoryTests {

    @Test
    public void getCachedInstancesTest() {
        Map<String, Object> cachedInstances = SpiExtensionFactory.getCachedInstances(SpiTest.class);
        assertEquals(2, cachedInstances.size());
    }

    interface SpiTest extends SpiIdentity {

        void hello(String msg);
    }

    public static class MyTest1 implements SpiTest {

        @Override
        public String type() {
            return "test1";
        }

        @Override
        public void hello(String msg) {
            System.out.println("test1 msg=" + msg);
        }
    }

    public static class MyTest2 implements SpiTest {

        @Override
        public String type() {
            return "test2";
        }

        @Override
        public void hello(String msg) {
            System.out.println("test2 msg=" + msg);
        }
    }

}
