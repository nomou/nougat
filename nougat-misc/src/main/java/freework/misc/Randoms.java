/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.misc;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 随机数工具类
 *
 * @author vacoor
 */
public abstract class Randoms {
    private static final char[] CHINESE_CHARS = ("的一了是我不在人们有来他这" +
            "上着个地到大里说就去子得也和那要下看天时过出小么起你都把好" +
            "还多没为又可家学只以主会样年想生同老中十从自面前头道它后然" +
            "走很像见两用她国动进成回什边作对开而己些现山民候经发工向事" +
            "命给长水几义三声于高手知理眼志点心战二问但身方实吃做叫当住" +
            "听革打呢真全才四已所敌之最光产情路分总条白话东席次亲如被花" +
            "口放儿常气五第使写军吧文运再果怎定许快明行因别飞外树物活部" +
            "门无往船望新带队先力完却站代员机更九您每风级跟笑啊孩万少直" +
            "意夜比阶连车重便斗马哪化太指变社似士者干石满日决百原拿群究" +
            "各六本思解立河村八难早论吗根共让相研今其书坐接应关信觉步反" +
            "处记将千找争领或师结块跑谁草越字加脚紧爱等习阵怕月青半火法" +
            "题建赶位唱海七女任件感准张团屋离色脸片科倒睛利世刚且由送切" +
            "星导晚表够整认响雪流未场该并底深刻平伟忙提确近亮轻讲农古黑" +
            "告界拉名呀土清阳照办史改历转画造嘴此治北必服雨穿内识验传业" +
            "菜爬睡兴形量咱观苦体众通冲合破友度术饭公旁房极南枪读沙岁线" +
            "野坚空收算至政城劳落钱特围弟胜教热展包歌类渐强数乡呼性音答" +
            "哥际旧神座章帮啦受系令跳非何牛取入岸敢掉忽种装顶急林停息句" +
            "区衣般报叶压慢叔背细").toCharArray();


    private static Random randomGenerator;

    /**
     * 随机产生一个 Boolean 值
     *
     * @return
     */
    public static boolean next() {
        return next(2) == 0;
    }

    /**
     * 随机产生给定数的正数或负数形式
     */
    public static int nextPositiveOrNegative(int seed) {
        ensureRandomGenerator();
        return randomGenerator.nextBoolean() ? seed : -seed;
    }

    /**
     * 随机产生 0 - seed(&lt;) 中任意一个数
     */
    public static int next(int seed) {
        return next(0, seed);
    }

    /**
     * 产生区间内的一个随机数, least &lt;= next &lt; bound
     */
    public static int next(int least, int bound) {
        ensureRange(least, bound);
        return randomGenerator.nextInt(bound - least) + least;
    }

    public static int[] next(int least, int bound, int len) {
        int[] ints = new int[len];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = next(least, bound);
        }
        return ints;
    }

    public static long next(long least, long bound) {
        ensureRange(least, bound);
        return randomGenerator.nextLong();
    }

    public static long[] next(long least, long bound, int len) {
        long[] r = new long[len];
        for (int i = 0; i < len; i++) {
            r[i] = next(least, bound);
        }
        return r;
    }

    public static float next(float least, float bound) {
        ensureRange(least, bound);
        return randomGenerator.nextFloat() * (bound - least) + least;
    }

    public static float[] next(float least, float bound, int len) {
        float[] r = new float[len];
        for (int i = 0; i < len; i++) {
            r[i] = next(least, bound);
        }
        return r;
    }

    public static double next(double least, double bound) {
        ensureRange(least, bound);
        return randomGenerator.nextDouble() * (bound - least) + least;
    }

    public static double[] next(double least, double bound, int len) {
        double[] r = new double[len];
        for (int i = 0; i < len; i++) {
            r[i] = next(least, bound);
        }
        return r;
    }

    /**
     * 给定数组中随机选取一个元素
     */
    public static <T> T next(T[] seed) {
        return seed[next(seed.length)];
    }

    /**
     * 给定数组中随机选取 len 个元素
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] next(T[] seed, int len) {
        T[] r = (T[]) Array.newInstance(seed.getClass().getComponentType(), len);
        for (int i = 0; i < r.length; i++) {
            r[i] = next(seed);
        }
        return r;
    }

    /**
     * 随机产生一个数字字符
     */
    public static char nextDigit() {
        // 0~9
        return (char) next(48, 58);
    }

    /**
     * 产生指定长度的随机数字字符
     */
    public static char[] nextDigit(int len) {
        char[] r = new char[len];
        for (int i = 0; i < len; i++) {
            r[i] = nextDigit();
        }
        return r;
    }

    /**
     * 产生随机字母
     */
    public static char nextLetter() {
        int next = next(65, 123 - 6);   // 去除大小写字母中间的6个字符
        return (char) (next > 90 ? next + 6 : next);
    }

    public static char[] nextLetter(int len) {
        char[] r = new char[len];
        for (int i = 0; i < len; i++) {
            r[i] = nextLetter();
        }
        return r;
    }

    /**
     * 产生一个字母或数字
     */
    public static char nextLetterOrDigit() {
        int next = next(48, 123 - 7 - 6);
        if (next > 57) next += 7;
        if (next > 90) next += 6;
        return (char) next;
    }

    public static char[] nextLetterOrDigit(int len) {
        char[] r = new char[len];
        for (int i = 0; i < len; i++) {
            r[i] = nextLetterOrDigit();
        }
        return r;
    }

    /**
     * 使用指定的字符种子生成指定长度的随机字符数组
     */
    public static char[] nextChars(char[] seed, int len) {
        char[] r = new char[len];
        for (int i = 0; i < r.length; i++) {
            r[i] = seed[next(seed.length)];
        }
        return r;
    }

    public static char nextChineseChar() {
        return nextChineseChars(1)[0];
    }

    public static char[] nextChineseChars(int len) {
        return nextChars(CHINESE_CHARS, len);
    }

    // --------------

    /**
     * 产生 \u4E00 ~ \u9FA0
     * unicode 中汉字范围为\u2E80-\u9FFF,但是\u2e80~\u4e00有些看起来是乱码
     */
    public static char[] nextChineseChars2(int len) {
        char[] r = new char[len];
        for (int i = 0; i < len; i++) {
            r[i] = (char) next('\u4E00', '\u9FA0');
        }
        return r;
    }

    private static void ensureRange(double least, double bound) {
        ensureRandomGenerator();
        if (least >= bound) {
            throw new IllegalArgumentException("least must be less than bound");
        }
    }

    private static synchronized void ensureRandomGenerator() {
        if (randomGenerator == null) {
            randomGenerator = new SecureRandom();
        }
    }
}
