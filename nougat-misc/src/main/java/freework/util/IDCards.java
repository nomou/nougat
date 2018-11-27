/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;


import freework.codec.MOD11_2;

/**
 * 身份证工具类
 * <p/>
 * 18位身份证编码规则：
 * 410381
 * 6位地址码 + 8位出生日期 + 3为顺序码 + 1位校验码
 * <p/>
 * 地址码:   区域编码,初次领证的地址，精确到区县；  //全国行政区划分编码
 * 出生日期: 四位年 两位月 两位日；
 * 顺序码:   当天出生顺序编码，奇数表男性，偶数表女性；
 * 顺序码:   在同一地址码所标识的区域范围内,对同年同月同日出生的人编定的顺序号,顺序码的奇数分配给男性，偶数分配给女性。
 * 校验码:   根据前十七位数字按照ISO 7064:1983.MOD 11-2校验码计算得出
 * <p/>
 * 备注说明：
 * 关于大陆身份证有的人会发现前几位为什么变化了.
 * 这主要出现在中国的重庆.原有的重庆人的身份证多数以51开头。
 * 以前隶属于四川的原因.但新办的身份证可能是50开头,原因是行政区划改变
 * 类似 河南新密 410183 --&gt; 410182
 * 所致.中国各地的行政区划代码请参考国家统计局网站。
 * <p/>
 *
 * @author vacoor
 */
public abstract class IDCards {
    /**
     * 最低年限
     */
    public static final int MIN = 1930;

    public static String to18(String idcard) {
        int len = null != idcard ? idcard.length() : 0;
        if (18 == len) {
            return idcard;
        }
        if (15 == len) {
            StringBuilder buffer = new StringBuilder(idcard);
            /**
             * 15位原来7、8位的年份号到2000年后攺为全称
             * 如1985年过去7、8位码是85,现在增改为1985,又在最后一位增加校验码，如后三位原来601，加一个5成为6015
             */
            buffer.insert(6, "19").append(MOD11_2.mod(buffer.substring(0, 17)));
            return buffer.toString();
        }
        throw new IllegalArgumentException("length must be is 15 or 18");
    }

    public static boolean check(String idcard) {
        int len = null != idcard ? idcard.length() : 0;
        if (18 == len) {
            return MOD11_2.check(to18(idcard));
        }
        throw new IllegalArgumentException("length must be is 18");
    }

    private IDCards() {
    }
}
