package org.gy.framework.util.http;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.gy.framework.util.regex.RegexUtils;

/**
 * @author gy
 */
@Slf4j
public class ClientIpUtils {

    private static final String HN_CND_SRC_IP = "cdn-src-ip";
    private static final String HN_X_REAL_IP = "x-real-ip";
    private static final String HN_X_FORWARDED_FOR = "x-forwarded-for";
    private static final String SR_IP = ",";
    private static final String IP_UNKNOWN = "127.0.0.1";

    public static String clientIp(HttpServletRequest request) {
        String ip = null;

        String cdnSrcIp = RequestUtils.headerValue(request, HN_CND_SRC_IP);
        String realIp = RequestUtils.headerValue(request, HN_X_REAL_IP);
        String forwardedFor = RequestUtils.headerValue(request, HN_X_FORWARDED_FOR);
        String remoteAddr = request.getRemoteAddr();

        log.debug("cdnSrcIp:{}, realIp:{}, forwardedFor:{}, remoteAddr:{}", cdnSrcIp, realIp, forwardedFor, remoteAddr);

        if (StringUtils.isNotBlank(cdnSrcIp)) {
            ip = ip(cdnSrcIp);
        } else if (StringUtils.isNotBlank(realIp)) {
            ip = ip(realIp);
        } else if (StringUtils.isNotBlank(forwardedFor)) {
            ip = ip(forwardedFor);
        } else if (StringUtils.isNotBlank(remoteAddr)) {
            ip = remoteAddr;
        }

        ip = ipValid(ip) ? ip : IP_UNKNOWN;

        log.debug("client_ip:{}", ip);

        return ip;
    }

    /**
     * 将ip地址转为整数形式
     *
     * @param ipStr ip地址字符串，不能为null
     * @return 返回ip地址的整数形式；如果ip地址格式不正确或者为null，返回0
     */
    public static long ipToLong(String ipStr) {
        if (Strings.isNullOrEmpty(ipStr) || !RegexUtils.isIp(ipStr)) {
            log.warn("invalid ip: {}", ipStr);
            return 0L;
        }
        long ipLong = 0L;
        String[] numbers = StringUtils.split(ipStr, '.');
        for (int i = 0; i < 4; ++i) {
            ipLong = ipLong << 8 | NumberUtils.toInt(numbers[i]);
        }
        return ipLong;
    }

    public static String longToIp(long ipLong) {
        return (ipLong >> 24) + "." + ((ipLong & 0xFFFFFF) >> 16) + "." + ((ipLong & 0xFFFF) >> 8) + "." + (ipLong
            & 0xFF);
    }

    private static String ip(String ips) {
        return StringUtils.split(ips, SR_IP)[0];
    }

    private static boolean ipValid(String ip) {
        return StringUtils.isNotBlank(ip) && InetAddresses.isInetAddress(ip);
    }

}
