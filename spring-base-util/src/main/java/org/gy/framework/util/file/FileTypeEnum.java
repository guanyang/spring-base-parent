package org.gy.framework.util.file;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public enum FileTypeEnum {

    //文件类型定义
    JPG("jpg", "ffd8ff", "image/jpeg"),

    JPEG("jpeg", "ffd8ff", "image/jpeg"),

    PNG("png", "89504e47", "image/png"),

    BMP("bmp", "424d", "image/bmp"),

    GIF("gif", "47494638", "image/gif");

    /**
     * 文件格式
     */
    private final String format;
    /**
     * 文件头字节码Hex
     */
    private final String headerHex;

    private final String contentType;

    public static final Map<String, FileTypeEnum> FORMAT_MAP = new HashMap<>();

    public static final char FILE_SPLIT = '.';

    public static final int BYTE_SIZE = 4;

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
     * 功能描述：检查文件格式跟文件字节码头是否匹配，true是，false否
     *
     * @param format 文件格式
     * @param is 文件流
     * @author gy
     * @version 1.0.0
     */
    public static boolean validateFileType(FileTypeEnum fileTypeEnum, byte[] src) {
        if (fileTypeEnum == null) {
            //只处理已经定义的格式，未定义的格式暂不处理
            return true;
        }
        String hex = getFileHeaderHex(src);
        if (StringUtils.isBlank(hex)) {
            return false;
        }
        return hex.startsWith(fileTypeEnum.getHeaderHex());
    }

    /**
     * 获取文件头部字节
     */
    public static String getFileHeaderHex(byte[] src) {
        //读取前四个字节，如果少于，则是非法内容
        if (src == null || src.length < BYTE_SIZE) {
            return null;
        }
        byte[] b = new byte[BYTE_SIZE];
        System.arraycopy(src, 0, b, 0, BYTE_SIZE);
        return Hex.encodeHexString(b);
    }
}
