package io.github.guanyang.util.file.support;

import java.io.InputStream;
import lombok.Data;
import lombok.experimental.Accessors;
import io.github.guanyang.util.file.FileTypeI;

/**
 * 功能描述：文件上传上下文
 *
 * @author gy
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class StreamFileContext {

    /**
     * 文件流
     */
    private InputStream inputStream;
    /**
     * 文件扩展名
     */
    private String format;
    /**
     * 是否开启文件内容违规检查过滤，true开启，false不开启，默认不开启
     */
    private boolean filterSupport = false;
    /**
     * 文件类型，仅内部传递
     */
    private FileTypeI fileType;

    public StreamFileContext(InputStream inputStream, String format) {
        this(inputStream, format, false);
    }

    public StreamFileContext(InputStream inputStream, String format, boolean filterSupport) {
        this.inputStream = inputStream;
        this.format = format;
        this.filterSupport = filterSupport;
    }
}
