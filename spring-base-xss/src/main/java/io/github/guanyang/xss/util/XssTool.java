package io.github.guanyang.xss.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * @author gy
 */
@Slf4j
public class XssTool {

    private static final Safelist SAFE_LIST = Safelist.relaxed();

    /**
     * 检查输入值是否包含XSS攻击代码
     *
     * @param value 待检查的字符串
     * @return 如果包含XSS攻击代码返回true，否则返回false
     */
    public static boolean matchXss(CharSequence value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        return !Jsoup.isValid(value.toString(), SAFE_LIST);
    }

}
