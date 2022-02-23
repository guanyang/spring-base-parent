package org.gy.framework.util.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

/**
 * 功能描述：图片处理工具类，参考文档：https://blog.csdn.net/meism5/article/details/94380007
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class ImageUtils {

    public static void main(String[] args) throws IOException {
        String format = "png";
        String srcImage = "/Users/gy/Downloads/0d6bac0a97b87d79fcbd58229604cc29.png";
//        String srcImage = "/Users/gy/Downloads/3a88d919d490ceb576876d294c68ceb9.jpeg";
        String targetImage = "/Users/gy/Downloads/" + UUID.randomUUID().toString() + "." + format;

        ImageType imageType = getImageType(new FileInputStream(srcImage));
        System.out.println("文件类型：" + imageType);

        makeWatermark(new FileInputStream(srcImage), new FileOutputStream(targetImage), "测试水印", format);
//        BufferedImage image = ImageIO.read(new FileInputStream(srcImage));
//        ByteArrayOutputStream bs = new ByteArrayOutputStream();
//        ImageIO.write(image, format, bs);
//        InputStream inputStream = new ByteArrayInputStream(bs.toByteArray());


        System.out.println("OK");



    }

    public static ImageType getImageType(InputStream is) {
        if (is == null) {
            return null;
        }
        byte[] b = new byte[4];
        try {
            is.read(b, 0, b.length);
        } catch (IOException e) {
            log.error("[getImageType]读取文件异常.", e);
        }
        String hexString = Hex.encodeHexString(b);
        return ImageType.validate(hexString);
    }

    public static void makeWatermark(InputStream is, OutputStream os, String text, String format) throws IOException {
        BufferedImage image = ImageIO.read(is);
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();

            //计算字体大小
            int fontSize = (int) (width * height * 0.000008 + 13);
            Font font = new Font(null, Font.PLAIN, fontSize);

            Graphics2D g = image.createGraphics();
            g.setFont(font);
            g.setColor(Color.white);

            //透明度
            float alpha = 0.9f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

            //计算水印坐标
            int x = width - getWatermarkLength(text, g) - 10;
            x = x < 0 ? 0 : x;
            int y = height - 10;
            y = y < 0 ? 0 : y;

            //对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawString(text, x, y);
            g.dispose();

            ImageIO.write(image, format, os);
        }

    }

    private static int getWatermarkLength(String text, Graphics2D g) {
        return g.getFontMetrics(g.getFont()).charsWidth(text.toCharArray(), 0, text.length());
    }

    @Getter
    @AllArgsConstructor
    public static enum ImageType {

        //图片枚举定义
        JPG("ffd8ff", "image/jpeg"),

        PNG("89504e47", "image/png"),

        GIF("47494638", "image/gif"),

        BMP("424d", "image/bmp");

        private String hexHeader;

        private String contentType;

        public static ImageType validate(String hexString) {
            if (StringUtils.isBlank(hexString)) {
                return null;
            }
            return Stream.of(values()).filter(e -> hexString.startsWith(e.getHexHeader())).findFirst().orElse(null);
        }

    }

}
