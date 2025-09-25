package io.github.guanyang.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class PNGEncoder {

    /**
     * PNG解码类 用于将数据写入PNG文件
     */
    private static final byte[] PNG_END = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x49, (byte) 0x45, (byte) 0x4E, (byte) 0x44};

    /**
     * 将字符串写入PNG文件
     *
     * @param f   PNG文件
     * @param str 需要写入的文本信息
     */
    public static void writePNG(File f, String str) {
        RandomAccessFile raf = null;
        FileInputStream tmpIn = null;
        FileOutputStream tmpOut = null;

        try {
            raf = new RandomAccessFile(f, "rw");
            File tmp = File.createTempFile("tmp", null);
            tmpIn = new FileInputStream(tmp);
            tmpOut = new FileOutputStream(tmp);

            //循环将PNG信息写入临时文件
            byte[] bbuf = new byte[12];
            int hasRead = 0;
            int i = 0;
            raf.seek(0);
            while ((hasRead = raf.read(bbuf)) > 0) {
                if (isEND(bbuf)) {
                    tmpOut.write(bbuf, 0, hasRead);
                    break;
                } else {
                    tmpOut.write(bbuf, 0, 1);
                }
                i = i + 1;
                raf.seek(i);
            }

            //将字符串追加到临时文件
            tmpOut.write(str.getBytes());

            //将临时文件内容写入PNG文件
            raf.close();
            f.delete();
            raf = new RandomAccessFile(f, "rw");
            bbuf = new byte[1024];
            raf.seek(0);
            while ((hasRead = tmpIn.read(bbuf)) > 0) {
                raf.write(bbuf, 0, hasRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                    raf = null;
                }
                if (tmpIn != null) {
                    tmpIn.close();
                    tmpIn = null;
                }
                if (tmpOut != null) {
                    tmpOut.close();
                    tmpOut = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否为结束符
     *
     * @param bbuf 需判断的byte[]
     * @return 是 true
     */
    private static boolean isEND(byte[] bbuf) {
        if (bbuf[0] == PNG_END[0] && bbuf[1] == PNG_END[1] && bbuf[2] == PNG_END[2] && bbuf[3] == PNG_END[3] &&
            bbuf[4] == PNG_END[4] && bbuf[5] == PNG_END[5] && bbuf[6] == PNG_END[6] && bbuf[7] == PNG_END[7]) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        writePNG(new File("/Users/gy/Downloads/test0041.png"), "aaaaaaaaaaaaaaaaa");
        System.out.println("ok");
    }

}
