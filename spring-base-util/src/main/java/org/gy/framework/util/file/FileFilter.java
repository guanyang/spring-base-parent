package org.gy.framework.util.file;

import java.io.IOException;
import org.gy.framework.util.file.enums.FileFilterTypeEnum;
import org.gy.framework.util.file.support.StreamFileContext;

/**
 * 功能描述：文件过滤处理
 *
 * @author gy
 * @version 1.0.0
 */
public interface FileFilter {

    /**
     * 功能描述：过滤器类型
     *
     * @author gy
     * @version 1.0.0
     */
    FileFilterTypeEnum type();

    /**
     * 功能描述：文件过滤处理
     *
     * @param context 文件流上下文
     * @author gy
     * @version 1.0.0
     */
    void doFilter(StreamFileContext context) throws IOException;

}
