package freework.misc;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * 图像操作工具类
 * <p/>
 * JDK 原生 ImageIO.read/write 不支持 cmyk 模式的 jpg会丢失 ICC 信息的图片(读取后有红色蒙版)
 * http://stackoverflow.com/questions/6829428/pure-java-alternative-to-jai-imageio-for-detecting-cmyk-images
 * 解决方案: <br />
 * 1. 使用 https://github.com/haraldk/TwelveMonkeys (实现 java image spi, 操作不用任何变化)
 * 2. GraphicsMagic + im4java (需要安装 GM, 效率高)
 *
 * @author vacoor
 */
public abstract class Images {
    /**
     * 水印绘制的位置.
     */
    public static int POS_TOP = 1;
    public static int POS_RIGHT = 1 << 1;
    public static int POS_BOTTOM = 1 << 2;
    public static int POS_LEFT = 1 << 3;

    static {
        ImageIO.scanForPlugins();
    }

    /**
     * 图片缩放.
     *
     * @param image 图片
     * @param w     目标宽度
     * @param h     目标高度
     * @param hints {@link Image#SCALE_FAST} {@link Image#SCALE_SMOOTH}
     * @return 缩放后的图片
     */
    public static Image scale(Image image, int w, int h, int hints) {
        // return image.getScaledInstance(w, h, Image.SCALE_FAST | Image.SCALE_SMOOTH);
        return image.getScaledInstance(w, h, hints);
    }

    public static BufferedImage drawWatermark(final BufferedImage image, final Image watermark, int position, Insets insets) {
        final BufferedImage mark = toBuffered(watermark);
        return drawWatermark(image, mark, mark.getWidth(), mark.getHeight(), position, insets);
    }

    public static BufferedImage drawWatermark(final BufferedImage image, final Image watermark, final int width, final int height, int position, Insets insets) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        int offsetX = (w - insets.left - insets.right - width) / 2;
        int offsetY = (h - insets.top - insets.bottom - height) / 2;

        int x = offsetX + insets.left;
        int y = offsetY + insets.top;
        if (0 != (POS_TOP & position)) {
            y -= offsetY;
        }
        if (0 != (POS_RIGHT & position)) {
            x += offsetX;
        }
        if (0 != (POS_BOTTOM & position)) {
            y += offsetY;
        }
        if (0 != (POS_LEFT & position)) {
            x -= offsetX;
        }

        final BufferedImage canvas = new BufferedImage(w, h, image.getType());
        final Graphics2D g2d = canvas.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.drawImage(watermark, x, y, width, height, null);
        g2d.dispose();
        return canvas;
    }

    /**
     * 在图片的给定位置绘制水印.
     *
     * @param originalImg 原始图片
     * @param watermark   水印图片
     * @param x           水印绘制的 X 坐标
     * @param y           水印绘制的 Y 坐标
     * @return 添加水印后的图片
     */
    public static BufferedImage drawWatermark(BufferedImage originalImg, Image watermark, int x, int y) {
        return drawWatermark(originalImg, watermark, x, y, -1, -1);
    }

    /**
     * 在图片给定区域绘制水印图片.
     *
     * @param originalImg 原始图片
     * @param watermark   水印图片
     * @param x           起始 x 坐标
     * @param y           起始 y 坐标
     * @param width       水印宽度
     * @param height      水印高度
     * @return 添加水印后的图片
     */
    public static BufferedImage drawWatermark(BufferedImage originalImg, Image watermark, int x, int y, int width, int height) {
        final int w = originalImg.getWidth();
        final int h = originalImg.getHeight();

        BufferedImage img = new BufferedImage(w, h, originalImg.getType());
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(originalImg, 0, 0, w - 1, h, null);
        if (0 < width && 0 < height) {
            g2d.drawImage(watermark, x, y, width, height, null);
        } else {
            g2d.drawImage(watermark, x, y, null);
        }
        g2d.dispose();

        return img;
    }

    public static BufferedImage read(File file) throws IOException {
        return ImageIO.read(file);
    }

    public static BufferedImage read(InputStream in) throws IOException {
        return ImageIO.read(in);
    }

    public static BufferedImage read(ImageInputStream iis) throws IOException {
        return ImageIO.read(iis);
    }

    public static boolean write(Image image, String format, File file) throws IOException {
        return ImageIO.write(toRendered(image), format, file);
    }

    public static boolean write(Image image, String format, OutputStream out) throws IOException {
        return ImageIO.write(toRendered(image), format, out);
    }

    public static boolean write(Image image, String format, ImageOutputStream out) throws IOException {
        return ImageIO.write(toRendered(image), format, out);
    }

    public static RenderedImage toRendered(Image image) {
        return null == image || image instanceof RenderedImage ? (RenderedImage) image : toBuffered(image);
    }

    /**
     * 将 Image 对象转换为指定透明度的 BufferedImage
     *
     * @param image
     * @return
     */
    public static BufferedImage toBuffered(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        return clone(image);
    }

    public static BufferedImage clone(Image image) {
        int w = image.getWidth(null);
        int h = image.getWidth(null);

        // unloaded
        if (0 >= w && 0 >= h) {
            final MediaTracker loader = new MediaTracker(new Component() {
            });
            final int id = (int) System.currentTimeMillis();

            try {
                loader.addImage(image, id);
                loader.waitForID(id);
                /*int status = */
                loader.statusID(id, false);
                w = image.getWidth(null);
                h = image.getHeight(null);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Can't load image: " + image);
            }
        }
        BufferedImage clone = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = clone.createGraphics();
        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.drawImage(image, 0, 0, w, h, null);
        g2d.dispose();

        return clone;
    }

    private static String getFormat(Object input) throws IOException {
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(input);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            iis.close();

            if (readers.hasNext()) {
                return readers.next().getFormatName();
            }
        } catch (Exception ignore) {
            // ignore
        }
        return null;
    }

    public static byte[] toBytes(Image img, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean write = ImageIO.write(toRendered(img), format, baos);
        return baos.toByteArray();
    }

    public static BufferedImage load(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return ImageIO.read(bais);
    }
}
