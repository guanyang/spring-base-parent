package io.github.guanyang.xss.util;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class ClassCheckUtils {

    private static final Set<Class> DEFAULT_BASIC_CLASS;

    static {
        DEFAULT_BASIC_CLASS = init();
    }

    private ClassCheckUtils() {
    }

    public static boolean checkBasicType(Class clazz) {
        return DEFAULT_BASIC_CLASS.contains(clazz);
    }

    private static Set<Class> init() {
        Set<Class> initSet = new HashSet<>();
        initSet.add(Boolean.class);
        initSet.add(Character.class);
        initSet.add(Byte.class);
        initSet.add(Short.class);
        initSet.add(Integer.class);
        initSet.add(Long.class);
        initSet.add(Float.class);
        initSet.add(Double.class);
        initSet.add(BigDecimal.class);
        initSet.add(BigInteger.class);
        initSet.add(String.class);
        initSet.add(StringBuffer.class);
        initSet.add(StringBuilder.class);
        initSet.add(byte.class);
        initSet.add(short.class);
        initSet.add(int.class);
        initSet.add(long.class);
        initSet.add(float.class);
        initSet.add(double.class);
        initSet.add(boolean.class);
        initSet.add(char.class);
        initSet.add(byte[].class);
        initSet.add(short[].class);
        initSet.add(int[].class);
        initSet.add(long[].class);
        initSet.add(float[].class);
        initSet.add(double[].class);
        initSet.add(boolean[].class);
        initSet.add(char[].class);
        initSet.add(Class.class);
        initSet.add(SimpleDateFormat.class);
        initSet.add(Currency.class);
        initSet.add(TimeZone.class);
        initSet.add(InetAddress.class);
        initSet.add(Inet4Address.class);
        initSet.add(Inet6Address.class);
        initSet.add(InetSocketAddress.class);
        initSet.add(File.class);
        initSet.add(Appendable.class);
        initSet.add(Charset.class);
        initSet.add(Pattern.class);
        initSet.add(Locale.class);
        initSet.add(URI.class);
        initSet.add(URL.class);
        initSet.add(UUID.class);
        initSet.add(AtomicBoolean.class);
        initSet.add(AtomicInteger.class);
        initSet.add(AtomicLong.class);
        initSet.add(AtomicReference.class);
        initSet.add(AtomicIntegerArray.class);
        initSet.add(AtomicLongArray.class);
        initSet.add(WeakReference.class);
        initSet.add(SoftReference.class);
        return initSet;
    }
}
