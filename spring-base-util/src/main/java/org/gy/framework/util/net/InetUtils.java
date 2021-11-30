package org.gy.framework.util.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gy
 */
@Slf4j
public class InetUtils {

    private static final String INTERFACE_NAME = "bond1";

    private static String ip = "127.0.0.1";

    static {
        try {
            ip = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .filter(f -> Arrays.asList(f.getDisplayName(), f.getName()).contains(INTERFACE_NAME))
                .flatMap(f -> Collections.list(f.getInetAddresses()).stream())
                .filter(f -> !f.isLoopbackAddress()).findFirst().orElse(InetAddress.getLocalHost())
                .getHostAddress();
        } catch (Exception e) {
            log.error("error when getting inet address.", e);
        }
    }

    public static String getIp() {
        return ip;
    }

}
