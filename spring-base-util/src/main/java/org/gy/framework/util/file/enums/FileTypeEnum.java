package org.gy.framework.util.file.enums;

import static org.gy.framework.util.file.enums.FileFilterTypeEnum.IMAGE_TYPE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.gy.framework.util.file.FileTypeI;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public enum FileTypeEnum implements FileTypeI {

    //文件类型定义
    JPG("jpg", "ffd8ff", "ffd9", "image/jpeg", true, IMAGE_TYPE),

    JPEG("jpeg", "ffd8ff", "ffd9", "image/jpeg", true, IMAGE_TYPE),

    PNG("png", "89504e47", "ae426082", "image/png", true, IMAGE_TYPE),

    BMP("bmp", "424d", null, "image/bmp", true, IMAGE_TYPE),

    GIF("gif", "47494638", "003b", "image/gif", false, IMAGE_TYPE);

    /**
     * 文件格式，小写
     */
    private final String format;
    /**
     * 文件头字节码Hex，为空则不进行校验
     */
    private final String headHex;
    /**
     * 文件尾字节码Hex，为空则不进行校验
     */
    private final String tailHex;

    private final String contentType;
    /**
     * 是否支持过滤清洗，目前gif图不支持
     */
    private final boolean filterSupport;

    private final FileFilterTypeEnum filterTypeEnum;

    private static final Map<String, FileTypeEnum> FORMAT_MAP = new HashMap<>();

    public static final char FILE_SPLIT = '.';

    static {
        for (FileTypeEnum item : values()) {
            FORMAT_MAP.put(item.getFormat(), item);
        }
    }

    public static FileTypeEnum formatOf(String format) {
        if (StringUtils.isBlank(format)) {
            return null;
        }
        return FORMAT_MAP.get(format.toLowerCase());
    }

    /**
     * 功能描述：检查文件格式跟文件字节码是否匹配，true是，false否
     *
     * @param format 文件格式
     * @param is 文件流
     * @author gy
     * @version 1.0.0
     */
    public static boolean validateFileType(FileTypeI fileTypeEnum, byte[] src) {
        if (fileTypeEnum == null) {
            //只处理已经定义的格式，未定义的格式暂不处理
            return true;
        }
        //文件头检查
        boolean checkHex = checkHex(src, fileTypeEnum.getHeadHex(), true);
        if (!checkHex) {
            return false;
        }
        //文件尾检查
        return checkHex(src, fileTypeEnum.getTailHex(), false);
    }

    /**
     * 功能描述：检查文件字节码是否匹配，true匹配，false不匹配
     *
     * @param src 源数据
     * @param hex 文件配置字节码hex
     * @param isheadCheck 是否头匹配，true是，false否则尾匹配
     * @author gy
     * @version 1.0.0
     */
    public static boolean checkHex(byte[] src, String hex, boolean isheadCheck) {
        if (src == null || src.length == 0) {
            return false;
        }
        // 未定义字节码Hex，则不进行校验，当做通过
        if (StringUtils.isBlank(hex)) {
            return true;
        }
        //校验文件Hex，两个hex字符占一个字节
        int len = (hex.length() + 1) / 2;
        int from = 0;
        BiFunction<String, String, Boolean> checkFun = StringUtils::startsWith;
        if (!isheadCheck) {
            from = src.length - len;
            checkFun = StringUtils::endsWith;
        }
        String result = readHex(src, from, from + len);
        return checkFun.apply(result, hex);
    }

    /**
     * 功能描述：从字节数组读取Hex字符串
     *
     * @param src 字节数组
     * @param from 起始
     * @param to 结束
     * @author gy
     * @version 1.0.0
     */
    public static String readHex(final byte[] src, final int from, final int to) {
        if (src == null || src.length == 0) {
            return StringUtils.EMPTY;
        }
        if ((to - from) <= 0) {
            return StringUtils.EMPTY;
        }
        byte[] bytes = Arrays.copyOfRange(src, from, to);
        return Hex.encodeHexString(bytes);
    }
}
