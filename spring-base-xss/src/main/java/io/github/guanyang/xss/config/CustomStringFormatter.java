package io.github.guanyang.xss.config;

import io.github.guanyang.xss.annotation.XssCheck;
import io.github.guanyang.xss.exception.XssException;
import io.github.guanyang.xss.util.XssTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;


/**
 * 字符串安全检查及去空格
 *
 * @author guanyang
 */
public class CustomStringFormatter implements Formatter<String> {

    private final boolean xssIgnore;

    private final boolean trimIgnore;

    public CustomStringFormatter() {
        this.xssIgnore = false;
        this.trimIgnore = false;
    }

    public CustomStringFormatter(boolean xssIgnore, boolean trimIgnore) {
        this.xssIgnore = xssIgnore;
        this.trimIgnore = trimIgnore;
    }

    public CustomStringFormatter(XssCheck annotation) {
        this.xssIgnore = !annotation.check();
        this.trimIgnore = !annotation.trim();
    }

    @Override
    public String parse(String text, Locale locale) throws ParseException {
        if (!xssIgnore && XssTool.matchXss(text)) {
            throw new XssException("Parameter safety check error", text);
        }
        return trimIgnore ? text : StringUtils.trim(text);
    }

    @Override
    public String print(String object, Locale locale) {
        return object;
    }
}
