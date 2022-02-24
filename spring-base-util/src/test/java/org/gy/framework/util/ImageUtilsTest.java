package org.gy.framework.util;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.gy.framework.util.file.ImageUtils;
import org.gy.framework.util.file.ImageUtils.ImageParam;
import org.junit.jupiter.api.Test;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class ImageUtilsTest {

    public static final String PAHT = "/Users/gy/Downloads/test";


    @Test
    public void makeWatermarkExeTest() throws IOException {
        List<String> list = buildTestList();
        for (String image : list) {
            makeWatermarkExe(image);
        }
    }

    @Test
    public void imageFilterExeTest() throws IOException {
        List<String> list = buildTestList();
        for (String image : list) {
            imageFilterExe(image);
        }
    }

    @Test
    public void thumbnailsExeTest() throws IOException {
        List<String> list = buildTestList();
        for (String image : list) {
            thumbnailsExe(image);
        }
    }

    public static List<String> buildTestList() {
        List<String> list = Lists.newArrayList();
        list.add(PAHT + "/test01.bmp");
        list.add(PAHT + "/test02.gif");
        list.add(PAHT + "/test03.jpg");
        list.add(PAHT + "/test04.png");
        list.add(PAHT + "/test05.wbmp");
        list.add(PAHT + "/test041.png");
        list.add(PAHT + "/test042.png");
        list.add(PAHT + "/test042_err.png");
        list.add(PAHT + "/test043_err.png");
        list.add(PAHT + "/test044.png");

        return list;
    }

    private static void makeWatermarkExe(String srcImage) throws IOException {
        String format = srcImage.substring(srcImage.lastIndexOf(".") + 1);
        String fileName = srcImage.substring(srcImage.lastIndexOf("/") + 1, srcImage.lastIndexOf("."));
        String targetImage = "/Users/gy/Downloads/test/makeWater_" + fileName + "_" + System.currentTimeMillis() + "." + format;

        ImageParam param = ImageParam.of().setText(".").setFormat(format);
        ImageUtils.makeWatermark(new FileInputStream(srcImage), new FileOutputStream(targetImage), param);

        log.info("文件地址：{}", targetImage);
    }

    private static void imageFilterExe(String srcImage) throws IOException {
        String format = srcImage.substring(srcImage.lastIndexOf(".") + 1);
        String fileName = srcImage.substring(srcImage.lastIndexOf("/") + 1, srcImage.lastIndexOf("."));
        String targetImage = "/Users/gy/Downloads/test/imageFilter_" + fileName + "_" + System.currentTimeMillis() + "." + format;

        BufferedImage image = ImageIO.read(new FileInputStream(srcImage));
        ImageIO.write(image, format, new FileOutputStream(targetImage));

        log.info("文件地址：{}", targetImage);

    }

    private static void thumbnailsExe(String srcImage) throws IOException {
        String format = srcImage.substring(srcImage.lastIndexOf(".") + 1);
        String fileName = srcImage.substring(srcImage.lastIndexOf("/") + 1, srcImage.lastIndexOf("."));
        String targetImage = "/Users/gy/Downloads/test/thumbnails_" + fileName + "_" + System.currentTimeMillis() + "." + format;

        Thumbnails.of(new FileInputStream(srcImage)).scale(1f).outputQuality(1f).outputFormat(format)
            .imageType(BufferedImage.TYPE_INT_ARGB).toOutputStream(new FileOutputStream(targetImage));

        log.info("文件地址：{}", targetImage);


    }

}
