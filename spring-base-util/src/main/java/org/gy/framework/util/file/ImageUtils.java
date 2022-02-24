package org.gy.framework.util.file;

import com.google.common.collect.Sets;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 功能描述：图片处理工具类，参考文档：https://blog.csdn.net/meism5/article/details/94380007
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class ImageUtils {

    public static final int BYTE_SIZE = 4;

    public static void main(String[] args) throws IOException {

//        GifDecoder decoder = new GifDecoder();
//        int status = decoder.read(new FileInputStream(srcImage));
//        if (status != GifDecoder.STATUS_OK) {
//            throw new IOException("read file " + srcImage + " error!");
//        }
//
//        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
//        encoder.start(new FileOutputStream(targetImage));
//        encoder.setRepeat(decoder.getLoopCount());
//        for (int i = 0; i < decoder.getFrameCount(); i ++) {
//            encoder.setDelay(decoder.getDelay(i));
//            BufferedImage childImage = decoder.getFrame(i);
//            encoder.addFrame(childImage);
//        }
//        encoder.finish();

        System.out.println("OK");


    }

    /**
     * 功能描述：水印处理
     *
     * @param is 输入流
     * @param os 输出流
     * @param param 图像参数
     * @author gy
     * @version 1.0.0
     */
    public static void makeWatermark(InputStream is, OutputStream os, ImageParam param) throws IOException {
        if (is == null || os == null) {
            throw new IllegalArgumentException("水印处理流错误");
        }
        if (param == null || StringUtils.isBlank(param.getText())) {
            throw new IllegalArgumentException("水印处理参数错误");
        }
        if (!ImageParam.SUPPORT_FORMAT.contains(param.getFormat())) {
            throw new IllegalArgumentException("水印处理图片格式不支持:" + param.getFormat());
        }
        BufferedImage image = ImageIO.read(is);
        if (image == null) {
            throw new IllegalArgumentException("水印处理图片不合法");
        }
        log.debug("[makeWatermark]图片水印处理：param={}", param);
        int width = image.getWidth();
        int height = image.getHeight();

        //计算字体大小
//        int fontSize = (int) (width * height * 0.000008 + 13);
//        Font font = new Font(null, Font.PLAIN, fontSize);

        Graphics2D g = image.createGraphics();
        //设置字体，颜色
        g.setFont(param.getFont());
        g.setColor(param.getColor());
        //对线段的锯齿状边缘处理
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //透明度
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, param.getAlpha()));

        //计算水印坐标
        String text = param.getText();
        int x = width - param.getPaddingRight();
        //自适应调整水印宽度
        if (param.isAutoTextWidth()) {
            x -= getWatermarkLength(text, g);
        }
        x = x < 0 ? 0 : x;
        int y = height - param.getPaddingBottom();
        y = y < 0 ? 0 : y;

        g.drawString(text, x, y);
        g.dispose();

        ImageIO.write(image, param.getFormat(), os);
    }


    /**
     * 计算水印文本宽度
     */
    private static int getWatermarkLength(String text, Graphics2D g) {
        return g.getFontMetrics(g.getFont()).charsWidth(text.toCharArray(), 0, text.length());
    }

    @Data
    @Accessors(chain = true)
    public static class ImageParam {

        public static final Font DEFAULT_FONT = new Font(null, Font.PLAIN, 12);

        public static final Color DEFAULT_COLOR = Color.WHITE;
        /**
         * 系统支持的图片类型
         */
        public static final Set<String> SUPPORT_FORMAT = Sets.newHashSet(ImageIO.getReaderFormatNames());

        /**
         * 图片格式类型，参考：ImageIO.getReaderFormatNames()
         */
        private String format;
        /**
         * 水印文字内容
         */
        private String text;
        /**
         * 自动调整水印显示宽度
         */
        private boolean autoTextWidth = false;
        /**
         * 字体
         */
        private Font font = DEFAULT_FONT;
        /**
         * 颜色
         */
        private Color color = DEFAULT_COLOR;
        /**
         * 透明度，范围[0.0,1.0]，值越小越透明
         */
        private float alpha;
        /**
         * 到右边距离
         */
        private int paddingRight;
        /**
         * 到底部距离
         */
        private int paddingBottom;

        public static ImageParam of() {
            return new ImageParam();
        }

    }

}
