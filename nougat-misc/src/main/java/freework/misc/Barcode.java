/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.misc;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import freework.util.Throwables;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 条码工具类
 *
 * @author vacoor
 */
public class Barcode {
    private static int FRAME_WIDTH = 2;
    private final String content;
    private final BufferedImage logo;

    /**
     * 创建一个条码对象
     *
     * @param content 条码内容
     * @return Barcode 实例
     */
    public static Barcode create(String content) {
        return create(content, null);
    }

    /**
     * 创建一个条码对象
     *
     * @param content 条码内容
     * @param logo    条码 Logo
     * @return Barcode 实例
     */
    public static Barcode create(String content, BufferedImage logo) {
        return new Barcode(content, logo);
    }

    /**
     * 创建一个条码对象
     *
     * @param content 条码内容
     * @param logo    条码 Logo
     */
    private Barcode(String content, BufferedImage logo) {
        this.content = content;
        this.logo = logo;
    }

    /**
     * 将当前条码数据转换为给定大小的图片
     *
     * @param w 图片宽度
     * @param h 图片高度
     * @return BufferedImage
     */
    public BufferedImage toBufferedImage(final int w, final int h) {
        try {
            int logoW = 0;
            int logoH = 0;
            int[][] logoPixels = new int[0][0];

            if (null != logo) {
                logoW = w / 4;
                logoH = h / 4;
                BufferedImage logo = scale(this.logo, logoW, logoH, true);

                logoPixels = new int[logoW][logoH];
                for (int x = 0; x < logo.getWidth(); x++) {
                    for (int y = 0; y < logo.getHeight(); y++) {
                        logoPixels[x][y] = logo.getRGB(x, y);
                    }
                }
            }

            BitMatrix matrix = encode(content, BarcodeFormat.QR_CODE, w, h);

            int halfW = matrix.getWidth() / 2;
            int halfH = matrix.getHeight() / 2;
            int logoHalfW = logoW / 2;
            int logoHalfH = logoH / 2;
            int[] pixels = new int[w * h];

            for (int y = 0; y < matrix.getHeight(); y++) {
                for (int x = 0; x < matrix.getWidth(); x++) {
                /*
                //左上角颜色,根据自己需要调整颜色范围和颜色
                if (x > 0 && x < 170 && y > 0 && y < 170) {
                    Color color = new Color(231, 144, 56);
                    int colorInt = color.getRGB();
                    pixels[y * width + x] = matrix.get(x, y) ? colorInt : 0xffffff;
                } else */
                    if (x > halfW - logoHalfW && x < halfW + logoHalfW && y > halfH - logoHalfH && y < halfH + logoHalfH) {
                        // 读取图片
                        pixels[y * w + x] = logoPixels[x - halfW + logoHalfW][y - halfH + logoHalfW];
                    } else if ((x > halfW - logoHalfW - FRAME_WIDTH && x < halfW - logoHalfW + FRAME_WIDTH && y > halfH - logoHalfH - FRAME_WIDTH && y < halfH + logoHalfH + FRAME_WIDTH) ||
                            (x > halfW + logoHalfW - FRAME_WIDTH && x < halfW + logoHalfW + FRAME_WIDTH && y > halfH - logoHalfH - FRAME_WIDTH && y < halfH + logoHalfH + FRAME_WIDTH) ||
                            (x > halfW - logoHalfW - FRAME_WIDTH && x < halfW + logoHalfW + FRAME_WIDTH && y > halfH - logoHalfH - FRAME_WIDTH && y < halfH - logoHalfH + FRAME_WIDTH) ||
                            (x > halfW - logoHalfW - FRAME_WIDTH && x < halfW + logoHalfW + FRAME_WIDTH && y > halfH + logoHalfH - FRAME_WIDTH && y < halfH + logoHalfH + FRAME_WIDTH)) {
                        // 在图片四周形成边框
                        // pixels[y * w + x] = 0xfffffff;
                        pixels[y * w + x] = WHITE;
                    } else {      //二维码颜色
                        /*
                        int r = (int) (50 - (50.0 - 13.0) / matrix.getHeight() * (y + 1));
                        int g = (int) (165 - (165.0 - 72.0) / matrix.getHeight() * (y + 1));
                        int b = (int) (162 - (162.0 - 107.0) / matrix.getHeight() * (y + 1));
                        Color color = new Color(r, g, b);
                        */
                        Color color = Color.BLACK;
                        int rgb = color.getRGB();      // 此处可以修改二维码的颜色，可以分别制定二维码和背景的颜色；
                        pixels[y * w + x] = matrix.get(x, y) ? rgb : WHITE;//0x000000:0xffffff
                    }
                }
            }

            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            image.getRaster().setDataElements(0, 0, w, h, pixels);
            return image;
        } catch (Exception e) {
            return Throwables.unchecked(e);
        }
    }

    /**
     * 把传入的原始图像按高度和宽度进行缩放，生成符合要求的图标
     *
     * @param img       源文件
     * @param height    目标高度
     * @param width     目标宽度
     * @param hasFiller 比例不对时是否需要补白：true为补白; false为不补白;
     * @throws IOException
     */
    private static BufferedImage scale(BufferedImage img, int height, int width, boolean hasFiller) throws IOException {
        double ratio; // 缩放比例
        Image destImage = img.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);   // 计算比例
        if ((img.getHeight() > height) || (img.getWidth() > width)) {
            if (img.getHeight() > img.getWidth()) {
                ratio = (new Integer(height)).doubleValue() / img.getHeight();
            } else {
                ratio = (new Integer(width)).doubleValue() / img.getWidth();
            }
            AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
            destImage = op.filter(img, null);
        }

        if (hasFiller) {// 补白
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();


            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, width, height);
            if (width == destImage.getWidth(null)) {
                g2d.drawImage(destImage, 0, (height - destImage.getHeight(null)) / 2, destImage.getWidth(null), destImage.getHeight(null), Color.white, null);
            } else {
                g2d.drawImage(destImage, (width - destImage.getWidth(null)) / 2, 0, destImage.getWidth(null), destImage.getHeight(null), Color.white, null);
            }
            g2d.dispose();
            destImage = image;
        }
        return Images.toBuffered(destImage);
    }

    public static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");
    public static final ErrorCorrectionLevel DEFAULT_LEVEL = ErrorCorrectionLevel.M;

    /**
     * 内容必须13位数字
     */
    public static BufferedImage encodeToEAN13(String content, int w, int h) {
        return toBufferedImage(encode(content, BarcodeFormat.EAN_13, w, h));
    }

    public static BufferedImage encodeToQRCode(String content, int w, int h) {
        return toBufferedImage(encode(content, BarcodeFormat.QR_CODE, w, h));
    }

    /* ***********************************
     *
     * ***********************************/

    public static BitMatrix encode(String content, BarcodeFormat format, int w, int h) {
        return encode(content, null, format, w, h);
    }

    public static BitMatrix encode(String content, Charset charset, BarcodeFormat format, int w, int h) {
        return encode(content, charset, null, format, w, h);
    }

    public static BitMatrix encode(String content, Charset charset, ErrorCorrectionLevel level, BarcodeFormat format, int w, int h) {
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        charset = null != charset ? charset : DEFAULT_CHARSET;
        level = null != level ? level : DEFAULT_LEVEL;
        hints.put(EncodeHintType.CHARACTER_SET, charset.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, level);
        // hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 0);
        return encode(content, format, w, h, hints);
    }

    public static BitMatrix encode(String content, BarcodeFormat format, int w, int h, Map<EncodeHintType, ?> hints) {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix;
        try {
            matrix = null != hints ? writer.encode(content, format, w, h, hints) : writer.encode(content, format, w, h);
        } catch (Exception ex) {
            matrix = handleBarcodeException(ex);
        }
        return matrix;
    }

    /* ********************************
     *
     * ********************************/

    public static String decode(BufferedImage bi) {
        return decode(bi, DEFAULT_CHARSET);
    }

    public static String decode(BufferedImage bi, Charset charset) {
        Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
        charset = null != charset ? charset : DEFAULT_CHARSET;
        hints.put(DecodeHintType.CHARACTER_SET, charset.name());
        return decode(bi, hints).getText();
    }

    public static Result decode(BufferedImage bi, Map<DecodeHintType, ?> hints) {
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bi);
        HybridBinarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap bitmap = new BinaryBitmap(binarizer);
        MultiFormatReader reader = new MultiFormatReader();
        Result result;
        try {
            result = (null != hints) ? reader.decode(bitmap, hints) : reader.decode(bitmap);
        } catch (Exception ex) {
            result = handleBarcodeException(ex);
        }
        return result;
    }

    private static <R> R handleBarcodeException(Exception ex) {
        if (ex instanceof NotFoundException) {
            IllegalStateException isEx = new IllegalStateException("encode error: " + ex.getMessage());
            isEx.setStackTrace(ex.getStackTrace());
            throw isEx;
        }
        if (ex instanceof ReaderException) {
            IllegalStateException isEx = new IllegalStateException("encode error: " + ex.getMessage());
            isEx.setStackTrace(ex.getStackTrace());
            throw isEx;
        }
        if (ex instanceof WriterException) {
            IllegalStateException isEx = new IllegalStateException("decode error: " + ex.getMessage());
            isEx.setStackTrace(ex.getStackTrace());
            throw isEx;
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new RuntimeException(ex);
    }

    /**
     *
     */
    private static final int BLACK = 0xff000000;
    private static final int WHITE = 0xffffffff;
    private static final int TRANSPARENT = 0x00ffffff;

    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : TRANSPARENT);
            }
        }
        return image;
    }

    protected static final class BufferedImageLuminanceSource extends LuminanceSource {
        private final BufferedImage image;
        private final int left;
        private final int top;

        public BufferedImageLuminanceSource(BufferedImage image) {
            this(image, 0, 0, image.getWidth(), image.getHeight());
        }

        public BufferedImageLuminanceSource(BufferedImage image, int left, int top, int width, int height) {
            super(width, height);

            int sourceWidth = image.getWidth();
            int sourceHeight = image.getHeight();
            if (left + width > sourceWidth || top + height > sourceHeight) {
                throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
            }

            for (int y = top; y < top + height; y++) {
                for (int x = left; x < left + width; x++) {
                    if ((image.getRGB(x, y) & 0xFF000000) == 0) {
                        image.setRGB(x, y, WHITE); // = white
                    }
                }
            }

            this.image = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_BYTE_GRAY);
            this.image.getGraphics().drawImage(image, 0, 0, null);
            this.left = left;
            this.top = top;
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            if (y < 0 || y >= getHeight()) {
                throw new IllegalArgumentException("Requested row is outside the image: " + y);
            }
            int width = getWidth();
            if (row == null || row.length < width) {
                row = new byte[width];
            }
            image.getRaster().getDataElements(left, top + y, width, 1, row);
            return row;
        }

        @Override
        public byte[] getMatrix() {
            int width = getWidth();
            int height = getHeight();
            int area = width * height;
            byte[] matrix = new byte[area];
            image.getRaster().getDataElements(left, top, width, height, matrix);
            return matrix;
        }

        @Override
        public boolean isCropSupported() {
            return true;
        }

        @Override
        public LuminanceSource crop(int left, int top, int width, int height) {
            return new BufferedImageLuminanceSource(image, this.left + left, this.top + top, width, height);
        }

        @Override
        public boolean isRotateSupported() {
            return true;
        }

        @Override
        public LuminanceSource rotateCounterClockwise() {
            int sourceWidth = image.getWidth();
            int sourceHeight = image.getHeight();

            AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, sourceWidth);

            BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, BufferedImage.TYPE_BYTE_GRAY);

            Graphics2D g = rotatedImage.createGraphics();
            g.drawImage(image, transform, null);
            g.dispose();

            int width = getWidth();
            return new BufferedImageLuminanceSource(rotatedImage, top, sourceWidth - (left + width), getHeight(), width);
        }
    }
}

