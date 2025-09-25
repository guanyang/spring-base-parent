package io.github.guanyang.util.file.support;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import io.github.guanyang.util.file.FileFilter;
import io.github.guanyang.util.file.enums.FileTypeEnum;
import io.github.guanyang.util.file.FileTypeI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public abstract class AbstractFileFilter implements FileFilter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFileFilter.class);

    @Override
    public void doFilter(StreamFileContext context) throws IOException {
        if (context == null || context.getInputStream() == null) {
            throw new IllegalArgumentException("文件上下文为空");
        }
        //只处理已经定义的格式，未定义的格式暂不处理
        FileTypeI fileTypeEnum = context.getFileType();
        //是否开启文件内容过滤，如果未开启，则不处理
        if (fileTypeEnum == null || !context.isFilterSupport()) {
            logger.debug("[FileFilter.doFilter]文件类型暂不处理:fileTypeEnum={}", fileTypeEnum);
            return;
        }
        //读取文件字节码，检查文件字节码是否匹配
        byte[] byteSrc = toByteArray(context.getInputStream());
        boolean fileTypeFlag = FileTypeEnum.validateFileType(fileTypeEnum, byteSrc);
        if (!fileTypeFlag) {
            throw new IllegalArgumentException("上传文件格式不合法:" + context.getFormat());
        }
        //原字节码回写输入流
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteSrc);
        context.setInputStream(inputStream);
        if (!fileTypeEnum.isFilterSupport()) {
            logger.debug("[FileFilter.doFilter]文件类型暂不支持过滤清洗:fileTypeEnum={}", fileTypeEnum);
            return;
        }
        doFileFilter(context);
    }

    private static byte[] toByteArray(final InputStream inputStream) throws IOException {
        try (final InputStream is = inputStream) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    /**
     * 文件内容过滤
     */
    protected abstract void doFileFilter(StreamFileContext context) throws IOException;

}
