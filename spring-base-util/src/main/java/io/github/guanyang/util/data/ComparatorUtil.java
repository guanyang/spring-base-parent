package io.github.guanyang.util.data;

import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class ComparatorUtil {

    public static final Comparator<String> DEFAULT = (v1, v2) -> compare(v1, v2);

    public static final Comparator<String> DEFAULT_REVERSED = DEFAULT.reversed();

    /**
     * 比较版本大小
     */
    public static int compare(String v1, String v2) {
        if (StringUtils.isBlank(v1) && StringUtils.isBlank(v2)) {
            return 0;
        }
        if (StringUtils.isBlank(v1)) {
            return -1;
        }
        if (StringUtils.isBlank(v2)) {
            return 1;
        }
        if (v1.equals(v2)) {
            return 0;
        }
        String[] versionArray1 = v1.split("\\.");
        String[] versionArray2 = v2.split("\\.");
        // 取数组最小长度值
        int minLength = Math.min(versionArray1.length, versionArray2.length);
        int idx = 0;
        int diff = 0;
        // 先比较长度，再比较字符
        while (idx < minLength && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0
            && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {
            ++idx;
        }
        // 如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        return diff;
    }

}
