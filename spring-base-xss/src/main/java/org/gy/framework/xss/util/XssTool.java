package org.gy.framework.xss.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gy
 */
@Slf4j
public class XssTool {

    private static Pattern scriptPatternAll = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);

    private static Pattern scriptPatternEnd = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);

    private static Pattern scriptPatternStart = Pattern.compile("<script(.*?)>",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static Pattern scriptPatternEval = Pattern.compile("eval\\((.*?)\\)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static Pattern scriptPatternExp = Pattern.compile("e­xpression\\((.*?)\\)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static Pattern scriptPatternJs = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);

    private static Pattern scriptPatternVb = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);

    private static Pattern scriptPatternOnload = Pattern.compile("onload(.*?)=",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static Pattern scriptPatternAlert = Pattern.compile("alert\\((.*?)\\)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static Pattern scriptPattern = Pattern.compile("<", Pattern.CASE_INSENSITIVE);


    public static boolean matchXSS(String value) {
        if (value != null) {
            // NOTE: It's highly recommended to use the ESAPI library and
            // uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);
            // Avoid anything between script tags
            Matcher result = scriptPatternAll.matcher(value);
            boolean flag = result.find();
            if (flag) {
                return true;
            }
            // Avoid anything in a
            // src="http://www.yihaomen.com/article/java/..." type of
            // e­xpression
            // scriptPattern =
            // Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
            // Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            // value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome </script> tag
            result = scriptPatternEnd.matcher(value);
            flag = result.find();
            if (flag) {
                return true;
            }
            // Remove any lonesome <script ...> tag
            result = scriptPatternStart.matcher(value);
            flag = result.find();
            if (flag) {
                return true;
            }
            // Avoid eval(...) e­xpressions
            result = scriptPatternEval.matcher(value);
            flag = result.find();
            if (flag) {
                return true;
            }

            result = scriptPatternExp.matcher(value);
            flag = result.find();
            if (flag) {
                return true;
            }

            // Avoid javascript:... e­xpressions
            result = scriptPatternJs.matcher(value);
            flag = result.find();
            if (flag) {
                return true;
            }

            // Avoid vbscript:... e­xpressions
            result = scriptPatternVb.matcher(value);
            flag = result.find();
            if (flag) {
                return true;
            }

            // Avoid onload= e­xpressions
            result = scriptPatternOnload.matcher(value);
            flag = result.find();
            if (flag) {
                return true;
            }

            // Avoid alert(...) e­xpressions
            result = scriptPatternAlert.matcher(value);
            flag = result.find();
            if (flag) {
                return true;
            }

            result = scriptPattern.matcher(value);
            flag = result.find();
            if (flag) {
                return true;
            }
        }
        return false;
    }
}
