package org.gy.framework.util.file.support;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.gy.framework.util.file.FileTypeI;
import org.gy.framework.util.file.enums.FileFilterTypeEnum;

/**
 * 功能描述：图片文件处理
 *
 * @author gy
 * @version 1.0.0
 */
public class ImageFileFilter extends AbstractFileFilter {

    @Override
    protected void doFileFilter(StreamFileContext context) throws IOException {
        FileTypeI fileTypeEnum = context.getFileType();

        //图片流清洗，剔除违规内容，可能会影响图片大小，适用于图片大小不是非常严格要求的场景
        BufferedImage image = ImageIO.read(context.getInputStream());
        //如果读取图片失败，或者高度、宽度为0，图片非法
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new IllegalArgumentException("图片内容不合法:" + context.getFormat());
        }
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageIO.write(image, fileTypeEnum.getFormat(), bs);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bs.toByteArray());

        //转换新的文件流
        context.setInputStream(inputStream);
    }

    @Override
    public FileFilterTypeEnum type() {
        return FileFilterTypeEnum.IMAGE_TYPE;
    }
}
