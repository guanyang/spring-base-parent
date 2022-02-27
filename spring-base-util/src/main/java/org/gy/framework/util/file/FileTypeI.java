package org.gy.framework.util.file;

import org.gy.framework.util.file.enums.FileFilterTypeEnum;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public interface FileTypeI {

    /**
     * 获取文件格式，小写
     *
     * @return String
     */
    String getFormat();

    /**
     * 文件头字节码Hex，为空则不进行校验
     *
     * @return String
     */
    String getHeadHex();

    /**
     * 文件尾字节码Hex，为空则不进行校验
     *
     * @return String
     */
    String getTailHex();

    /**
     * 文件类型
     *
     * @return String
     */
    String getContentType();

    /**
     * 是否支持过滤清洗
     *
     * @return boolean
     */
    boolean isFilterSupport();

    /**
     * 文件过滤器类型
     *
     * @return FileFilterTypeEnum
     */
    FileFilterTypeEnum getFilterTypeEnum();

}
