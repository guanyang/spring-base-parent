package io.github.guanyang.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import io.github.guanyang.util.file.FileFilter;
import io.github.guanyang.util.file.FileFilterFactory;
import io.github.guanyang.util.file.enums.FileTypeEnum;
import io.github.guanyang.util.file.support.StreamFileContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class FileFilterTest {

    private static final String PATH = "/Users/gy/Downloads/test/";

    @Test
    public void doFilterTest() throws IOException {
        String srcImage = PATH + "test042.png";
        imageFilterExe(srcImage);
    }

    @Test
    public void doFilterExceptionTest() throws IOException {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String srcImage = PATH + "test043_err.png";
            imageFilterExe(srcImage);
        });
        Assertions.assertNotNull(exception.getMessage());
    }

    private static void imageFilterExe(String srcImage) throws IOException {
        String format = srcImage.substring(srcImage.lastIndexOf(".") + 1);
        String fileName = srcImage.substring(srcImage.lastIndexOf("/") + 1, srcImage.lastIndexOf(".")) + "_" + System
            .currentTimeMillis() + "." + format;
        String targetImage = PATH + fileName;
        StreamFileContext context = new StreamFileContext(new FileInputStream(srcImage), format, true);
        FileTypeEnum typeEnum = FileTypeEnum.formatOf(format);
        context.setFileType(typeEnum);
        //执行过滤逻辑
        FileFilter fileFilter = FileFilterFactory.findFilter(typeEnum.getFilterTypeEnum());
        fileFilter.doFilter(context);
        IOUtils.copy(context.getInputStream(), new FileOutputStream(targetImage), IOUtils.DEFAULT_BUFFER_SIZE);

        System.out.println("文件地址：" + targetImage);

    }

}
