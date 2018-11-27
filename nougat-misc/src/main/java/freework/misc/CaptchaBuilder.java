/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.misc;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * 简单验证码生成器
 * <p>
 * 推荐尺寸:
 * 最小: 80x30, 90x35
 * 最佳: 120x40
 * 最大: 180x50
 * <p>
 * 过大将太耗时
 * TODO 随机数用的太多了, 稍后优化下 搞定 88 x36 6位
 *
 * @author vacoor
 */
class CaptchaBuilder {
    /*- 默认字体列表 */
    public static final Font[] DEFAULT_FONTS = {
            new Font("Helvetica", Font.BOLD, 22),
            new Font("Arial", Font.BOLD, 22),
            new Font("Sans-Serif", Font.BOLD, 22),
    };

    /*- 默认背景 */
    public static final Color[] DEFAULT_BACKGROUND = {new Color(255, 255, 255)};

    /*- 旋转的最大角度 */
    private static final int DEFAULT_MAX_ROTATE_ANGLE = 45;
    private static final int DEFAULT_WIDTH = 75;
    private static final int DEFAULT_HEIGHT = 25;

    private Font[] fonts = DEFAULT_FONTS;
    private Color[] backgrounds = DEFAULT_BACKGROUND;
    private int maxRotateAngle = DEFAULT_MAX_ROTATE_ANGLE;
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private boolean autoFontSize;
    private char[] chars;

    public static CaptchaBuilder create() {
        return new CaptchaBuilder();
    }

    public BufferedImage build() {
        if (chars == null || chars.length < 1) {
            throw new IllegalStateException("Captcha chars is not setting, you must be call setChars(char[])");
        }

        BufferedImage captcha = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = captcha.createGraphics();
        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // 不透明度
        // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));

        // 随机字体
        Font font = Randoms.next(fonts);
        if (autoFontSize) { // 计算合适字体
            float fontSize = (fontSize = Math.min(width / chars.length, height)) > 10 ? fontSize : 10;
            // 英文最好大一点
//            float fontSize = (fontSize = Math.min(width / chars.length, height)) > 10 ? fontSize + 5 : 10;
            font = font.deriveFont(fontSize);
        }
        g2d.setFont(font);
        FontRenderContext renderContext = g2d.getFontRenderContext();

        // 获取字符可视区域大小 (获得bounds 中 x, y 为相对基线)
        Rectangle2D charsBounds = font.layoutGlyphVector(renderContext, chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT).getVisualBounds();
        final double gap = (width - charsBounds.getWidth()) / (chars.length + 1);
        final double charWidth = charsBounds.getWidth() / chars.length;

        Rectangle rect = new Rectangle(0, 0, width, height);
        paintBackground(g2d, rect);  // 背景
        paintNoise(g2d, rect);  // 噪点
        paintCurve(g2d, rect); // 干扰线

        // 绘制内容
        double x = gap;
        double y = height / 2.0;

        // 字体竖直方向居中时基线 y 坐标 = 图片中心 y 坐标 - 字体中心相对字体基线偏差
        final int fY = (int) (y - charsBounds.getCenterY());
        Stroke stroke = new BasicStroke(1.2f);
        g2d.setStroke(stroke);

        for (int i = 0; i < chars.length; i++) {
            // 随机颜色
            int[] rgb = Randoms.next(1, 120, 3);
            g2d.setColor(new Color(rgb[0], rgb[1], rgb[2]));

            // 随机旋转的弧度
            double rotate = Randoms.nextPositiveOrNegative(DEFAULT_MAX_ROTATE_ANGLE) / 180.0 * Math.PI;

            /*- 缩放,效果不是很好 */
            // float scale = randomInt.nextInt(50) / 100F + 0.8F;
            // transform.scale(scale, scale);

            AffineTransform transform = AffineTransform.getRotateInstance(rotate, x, y);
            /*- 镂空后台难看
            if (Randoms.next() && charWidth > 13) {    // outline
                transform.translate(x, fY);
                TextLayout textLayout = new TextLayout("" + chars[i], g2d.getFont(), renderContext);
                Shape outline = textLayout.getOutline(transform);
                g2d.draw(outline);
            } else {
            */
            AffineTransform oldTransform = g2d.getTransform();

            g2d.setTransform(transform);
            g2d.drawChars(chars, i, 1, (int) x, fY);    // 旋转后已经有偏移, 这里不再添加偏移量

            g2d.setTransform(oldTransform);
            // }
            x += charWidth + gap;
        }
        g2d.dispose();

        return captcha;
    }

    /**
     * 在给定区域绘制背景
     */
    protected void paintBackground(Graphics2D g2d, Rectangle rect) {
        Color oldColor = g2d.getColor();
        g2d.setColor(Randoms.next(DEFAULT_BACKGROUND));
        g2d.fillRect(rect.x, rect.y, rect.width, rect.height);

        g2d.setColor(oldColor);
    }

    /**
     * 在指定区域绘制噪点
     */
    protected void paintNoise(Graphics2D g2d, Rectangle rect) {
        Font font = g2d.getFont();
        Color color = g2d.getColor();
        g2d.setFont(font.deriveFont(Font.BOLD, (float) rect.getHeight() / 3f));

        // 绘制40个噪点
        for (int i = 0; i < 8; i++) {
            // g2d.setColor(new Color(randomInt(150, 225), randomInt(150, 225), randomInt(150, 225)));
            int[] rgb = Randoms.next(150, 200, 3);
            g2d.setColor(new Color(rgb[0], rgb[1], rgb[2]));
            for (int j = 0; j < 5; j++) {  // 每种颜色绘制3个噪点
                int x = rect.x + Randoms.next(rect.width);
                int y = rect.y + Randoms.next(rect.height);
                g2d.drawChars(Randoms.nextLetterOrDigit(1), 0, 1, x, y);
            }
        }

        g2d.setFont(font);
        g2d.setColor(color);
    }

    /**
     * 给定区域绘制干扰线, 暂时就直线
     */
    protected void paintCurve(Graphics2D g2d, Rectangle rect) {
        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g2d.getColor();

        g2d.setStroke(new BasicStroke(rect.height / 3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
        // g2d.setColor(new Color(randomInt(150, 225), randomInt(150, 225), randomInt(150, 225)));
        int[] rgb = Randoms.next(150, 200, 3);
        int offsetY = Randoms.next(rect.height);

        g2d.setColor(new Color(rgb[0], rgb[1], rgb[2]));
        g2d.drawLine(rect.x, rect.y + offsetY, rect.x + rect.width, rect.y + rect.height - offsetY);

        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);
    }

    /* ********************************
     *          getter / setter
     * ********************************/
    public Font[] getFonts() {
        return fonts;
    }

    public CaptchaBuilder setFonts(Font[] fonts) {
        this.fonts = fonts;
        return this;
    }

    public Color[] getBackgrounds() {
        return backgrounds;
    }

    public CaptchaBuilder setBackgrounds(Color[] backgrounds) {
        this.backgrounds = backgrounds;
        return this;
    }

    public int getMaxRotateAngle() {
        return maxRotateAngle;
    }

    public CaptchaBuilder setMaxRotateAngle(int maxRotateAngle) {
        this.maxRotateAngle = maxRotateAngle;
        return this;
    }

    public char[] getChars() {
        return chars;
    }

    public CaptchaBuilder setChars(char[] chars) {
        this.chars = chars;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public CaptchaBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public CaptchaBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public boolean isAutoFontSize() {
        return autoFontSize;
    }

    public CaptchaBuilder setAutoFontSize(boolean autoFontSize) {
        this.autoFontSize = autoFontSize;
        return this;
    }

    private CaptchaBuilder() {
    }
}
