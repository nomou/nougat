/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 农历日历(1900~2050)
 * <p>
 * 修改自 网络源码作者不详
 */
/* 阴历每逢57年完全一样重复一次 */
public class LunarCalendar {
    private static final int START_YEAR = 1900;     // 数据起始年份
    private static final int END_YEAR = 2050;       // 数据结束年份
    private static final long START_TIME_OFFSET = -2206425952000L;  // 1900.1.31 相对于1970.1.1 时间偏移量

    private final static long[] LUNAR_YEAR_INFO = new long[]{
    /*
     * 1900~2050年数据 每年数据使用一个long存储,使用低17位存放数据 第17位(0x10000)为1则为闰月,为30天否则为29
	 * 16~5(0x8000~>0x8)存放当年每个阴历月是大月(30天)还是小月(29天),用1/0表示,依次存放1~12月
	 * 低4位(0xF)闰月的月份(1~12)没有则为0
	 */
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554,
            0x056a0, 0x09ad0, 0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250,
            0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970,
            0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570,
            0x052f2, 0x04970, 0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0,
            0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6,
            0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950,
            0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573,
            0x052d0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60,
            0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
            0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558,
            0x0b540, 0x0b5a0, 0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0,
            0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5,
            0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60,
            0x096d5, 0x092e0, 0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552,
            0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0,
            0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0,
            0x0a930, 0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6,
            0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0,
            0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
            0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50,
            0x1b255, 0x06d20, 0x0ada0};

    // 农历年的属相(生肖)
    private static final String[] ZODIAC = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
    private static final String[] CHINESE_NUMBER = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二", "初", "十", "廿", "卅"};
    private static final String[] Gan = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    private static final String[] Zhi = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};

    private int year;
    private int lunarMonth;
    private int lunarDay;

    public LunarCalendar(Calendar cal) {
        year = cal.get(Calendar.YEAR);
        Date date = cal.getTime();
        int offsetMonth = -1;

        // 求出当前时间相对于1900.1.31(农历1.1)的天数偏移量
        int offsetDay = (int) ((date.getTime() - START_TIME_OFFSET) / (24 * 60 * 60 * 1000));

        // 将天数偏移转换为 年偏移,月偏移,日偏移
        for (int y = START_YEAR; y < END_YEAR && offsetDay > 1; y++) {
            // 获取当前公历年份的农历年的总天数
            int days = getLunarYearDays(y);
            // 如果超过整年
            if (offsetDay - days >= 0) {
                offsetDay -= days;
            } else {
                // 将日偏差转换为月偏差
                for (int m = 1; m < 13 && offsetDay > 0; m++) {
                    days = getLunarMonthDays(y, m);
                    offsetMonth += 1;
                    if (offsetDay - days >= 0) {
                        offsetDay -= days;
                    } else {
                        break;
                    }
                    if (m == getLeapMonth(y)) {
                        days = getLeapMonthDays(y);
                        if (offsetDay - days >= 0) {
                            offsetDay -= days;
                        } else {
                            break;
                        }
                    }
                    if (0 == offsetDay) {
                        offsetMonth++;
                    }
                }
                break;
            }
        }
        // 农历月份 = 阴历数据起始月份(1月) + 月偏移
        lunarMonth = 1 + offsetMonth;
        lunarDay = 1 + offsetDay;
    }

    /**
     * 获取当前公历年的农历年的天数
     */
    private int getLunarYearDays(int y) {
        // 先按照小月算
        int sum = 29 * 12;
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            // 如果是大月则加1
            if (0 != (LUNAR_YEAR_INFO[y - START_YEAR] & i)) {
                sum += 1;
            }
        }
        sum += getLeapMonthDays(y);
        return sum;
    }

    /**
     * 获取某一年中阴历闰月的天数,如果没有闰月则返回0
     *
     * @param y
     * @return
     */
    private int getLeapMonthDays(int y) {
        // 如果存在闰月
        if (0 != getLeapMonth(y)) {
            // 第17二进制位标志闰月是大月还是小月
            return (0 != (LUNAR_YEAR_INFO[y - START_YEAR] & 0x10000)) ? 30 : 29;
        }
        return 0;
    }

    /**
     * 获取某年农历月的天数
     *
     * @param y  公历年份
     * @param lm 农历月份
     * @return 农历月份的天数
     */
    public int getLunarMonthDays(int y, int lm) {
        return (0 == (LUNAR_YEAR_INFO[y - START_YEAR] & (0x10000 >> lm)) ? 29 : 30);
    }

    /**
     * 获取某一年中闰月的阴历月份,如果没有则返回0
     *
     * @param y
     * @return
     */
    private int getLeapMonth(int y) {
        return (int) (LUNAR_YEAR_INFO[y - START_YEAR] & 0xF);
    }

	/* ------------ 以下为共有方法 ----------------- */

    /**
     * 获取干支
     *
     * @return
     */
    public final String getCyclical() {
        return getCyclicalm(year - START_YEAR + 36);
    }

    // 传入 月日的 offset 传回干支, 0=甲子 ??????????????
    private final static String getCyclicalm(int num) {
        return (Gan[num % 10] + Zhi[num % 12]);
    }

    /**
     * 获取公历年的农历年属相(生肖)
     */
    public String getZodiacYear() {
        return ZODIAC[(year - 4) % 12];
    }

    /**
     * 获取农历月
     *
     * @return
     */
    public String getLunarMonth() {
        return (CHINESE_NUMBER[lunarMonth - 1] + "月").replaceAll("^一", "正").replaceAll("十二", "腊");
    }

    /**
     * 获取农历日
     *
     * @return
     */
    public String getLunarDay() {
        int n = lunarDay % 10 == 0 ? 9 : lunarDay % 10 - 1;
        // 如果是初十则会为"十十"
        return (CHINESE_NUMBER[lunarDay / 10 + 12] + CHINESE_NUMBER[n]).replaceAll("^十十", "初十").replaceAll("^廿十", "廿").replaceAll("^卅十", "卅");
    }

    public String toString() {
        return getCyclical() + "[" + getZodiacYear() + "]年 " + getLunarMonth() + getLunarDay();
    }
}
