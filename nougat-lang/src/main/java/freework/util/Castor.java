/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Castor.
 *
 * @author vacoor
 * @since 1.0
 */

public abstract class Castor {
    private static final String DATE_FORMAT_PLAIN = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String DATE_FORMAT_ISO8601_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String DATE_FORMAT_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Non-instantiate.
     */
    private Castor() {
    }

    /**
     * Casts given value to type.
     *
     * @param value the value
     * @param type  the type class
     * @param <T>   the type
     * @return the converted value
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object value, final Class<T> type) {
        if (null == value) {
            return null;
        }
        if (Boolean.TYPE.equals(type) || Boolean.class.equals(type)) {
            return (T) asBoolean(value);
        }
        if (Character.TYPE.equals(type) || Character.class.equals(type)) {
            final String literal = String.valueOf(value);
            return (T) (0 < literal.length() ? literal.charAt(0) : null);
        }
        if (Byte.TYPE.equals(type) || Byte.class.equals(type)) {
            return (T) Castor.asByte(value);
        }
        if (Short.TYPE.equals(type) || Short.class.equals(type)) {
            return (T) Castor.asShort(value);
        }
        if (Integer.TYPE.equals(type) || Integer.class.equals(type)) {
            return (T) Castor.asInt(value);
        }
        if (Long.TYPE.equals(type) || Long.class.equals(type)) {
            return (T) Castor.asLong(value);
        }
        if (Float.TYPE.equals(type) || Float.class.equals(type)) {
            return (T) Castor.asFloat(value);
        }
        if (Double.TYPE.equals(type) || Double.class.equals(type)) {
            return (T) Castor.asDouble(value);
        }
        if (String.class.equals(type)) {
            return (T) value;
        }
        throw newClassCastException(value, type);
    }

    /**
     * Converts given object to string.
     *
     * @param obj the object
     * @return null if object is null, otherwise converted value
     */
    public static String asString(final Object obj) {
        if (null == obj) {
            return null;
        }
        // FIXED 'mmm,nnn' or '10E5'
        if (obj instanceof Number) {
            final NumberFormat fmt = NumberFormat.getInstance();
            fmt.setGroupingUsed(false);
            return fmt.format((Number) obj);
        }
        return obj.toString();
    }

    /**
     * Convert given object to Boolean.
     * <p>
     * return true if obj.toString is true|y|yes|on|1
     * return false if obj.toString is false|n|no|off|0
     *
     * @param obj the object
     * @return the boolean
     */
    public static Boolean asBoolean(final Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }

        if (obj instanceof Number) {
            int i = ((Number) obj).intValue();
            if (0 == i) {
                return Boolean.FALSE;
            }
            if (1 == i) {
                return Boolean.TRUE;
            }
        }

        String value = asString(obj);

        if (value == null || 0 == value.length()) {
            return null;
        }

        if ("true".equalsIgnoreCase(value) ||
                "t".equalsIgnoreCase(value) ||
                "1".equalsIgnoreCase(value) ||
                "enabled".equalsIgnoreCase(value) ||
                "y".equalsIgnoreCase(value) ||
                "yes".equalsIgnoreCase(value) ||
                "on".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }

        if ("false".equalsIgnoreCase(value) ||
                "f".equalsIgnoreCase(value) ||
                "0".equalsIgnoreCase(value) ||
                "disabled".equalsIgnoreCase(value) ||
                "n".equalsIgnoreCase(value) ||
                "no".equalsIgnoreCase(value) ||
                "off".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }

        throw newClassCastException(obj, Boolean.class);
    }

    /**
     * Converts given object to Byte value.
     *
     * @param obj the object
     * @return null if object is null, otherwise converted value
     */
    public static Byte asByte(final Object obj) {
        return asNumber(obj, Byte.class);
    }

    /**
     * Converts given object to Short value.
     *
     * @param obj the object
     * @return null if object is null, otherwise converted value
     */
    public static Short asShort(Object obj) {
        return asNumber(obj, Short.class);
    }

    /**
     * Converts given object to Integer value.
     *
     * @param obj the object
     * @return null if object is null, otherwise converted value
     */
    public static Integer asInt(Object obj) {
        return asNumber(obj, Integer.class);
    }

    /**
     * Converts given object to Long value.
     *
     * @param obj the object
     * @return null if object is null, otherwise converted value
     */
    public static Long asLong(Object obj) {
        return asNumber(obj, Long.class);
    }

    /**
     * Converts given object to Float value.
     *
     * @param obj the object
     * @return null if object is null, otherwise converted value
     */
    public static Float asFloat(Object obj) {
        return asNumber(obj, Float.class);
    }

    /**
     * Converts given object to Double value.
     *
     * @param obj the object
     * @return null if object is null, otherwise converted value
     */
    public static Double asDouble(final Object obj) {
        return asNumber(obj, Double.class);
    }

    /**
     * Converts given object to Number subclass.
     *
     * @param obj         the object
     * @param targetClass the target type class
     * @param <N>         the target type
     * @return null if object is null, otherwise converted value
     */
    @SuppressWarnings("unchecked")
    public static <N extends Number> N asNumber(final Object obj, final Class<N> targetClass) {
        if (null == obj) {
            return null;
        }

        String text;
        if (obj instanceof Boolean) {
            text = (Boolean) obj ? "1" : "0";
        } else if (obj instanceof Character) {
            text = Integer.valueOf(((Character) obj).charValue()).toString();
        } else {
            text = asString(obj);
            if (text == null || 0 == text.length()) {
                return null;
            }
        }

        try {
            if (Byte.TYPE.equals(targetClass) || Byte.class.equals(targetClass)) {
                return (N) Byte.decode(text);
            }
            if (Short.TYPE.equals(targetClass) || Short.class.equals(targetClass)) {
                return (N) Short.decode(text);
            }
            if (Integer.TYPE.equals(targetClass) || Integer.class.equals(targetClass)) {
                return (N) Integer.decode(text);
            }
            if (Long.TYPE.equals(targetClass) || Long.class.equals(targetClass)) {
                return (N) Long.decode(text);
            }
            if (Float.TYPE.equals(targetClass) || Float.class.equals(targetClass)) {
                return (N) Float.valueOf(text);
            }
            if (Double.TYPE.equals(targetClass) || Double.class.equals(targetClass)) {
                return (N) Double.valueOf(text);
            }
            if (BigInteger.class.equals(targetClass)) {
                return (N) new BigInteger(text);
            }
            if (BigDecimal.class.equals(targetClass) || Number.class.equals(targetClass)) {
                return (N) new BigDecimal(text);
            }
        } catch (final NumberFormatException e) {
            throw newClassCastException(obj, Number.class);
        }
        throw newClassCastException(obj, Number.class);
    }

    /**
     * 该方法提供类似 JavaScript 的parse* 的从10进制字符串中解析数值
     * 两种模式: 严格和宽松
     * <p>
     * 宽松模式: parseNumber("12a3", false) --&gt; 12, 尽可能多的匹配数值
     * <p>
     *
     * @param text
     * @return
     */
    public static Number parseNumber(String text) {
        // 全部转换为大写 e --&gt; E
        StringCharacterIterator it = new StringCharacterIterator(text.trim().toUpperCase(Locale.ENGLISH));
        long number = 0;
        StringBuilder buffer = null;
        boolean negative = false;

        // 提取整数循环
        longLoop:
        while (it.getIndex() < it.getEndIndex()) {
            char c = it.current();
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    number *= 10;
                    number += (c - '0');
                    it.next();
                    break;
                case '-':
                case '+':
                    if (0 != number) {
                        break longLoop;
                    }
                    negative = '-' == c;
                    it.next();
                    break;
                // double 标志
                case '.':
                case 'E': // 'e', 'E'
                    buffer = new StringBuilder(16);
                    buffer.append(negative ? '-' : "").append(number).append(c);
                    it.next();
                    break longLoop;
                default:
                    it.next();
                    break longLoop;
            }
        }

        if (buffer == null) {    // 只有整数部分
            return negative ? -1 * number : number;
        }

        final boolean dot = '.' == buffer.charAt(buffer.length() - 1); // '.', 'e', 'E'
        doubleLoop:
        while (it.getIndex() < it.getEndIndex()) {
            char ch = it.current();

            switch (ch) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    buffer.append(ch);
                    it.next();
                    break;
                case '+':
                case '-':
                    // 只允许 E 后面出现 + / -
                    if ('E' != buffer.charAt(buffer.length() - 1)) {
                        break doubleLoop;
                    }
                    buffer.append('-' == ch ? ch : "");
                    it.next();
                    break;
                case '.':
                case 'E':
                    // 如果 . 前面应出现过 . 或 E
                    if (('.' == ch && dot) || ('E' == ch && !dot)) {
                        // ------
                        break doubleLoop;
                    }
                    buffer.append(ch);
                    it.next();
                default:
                    break doubleLoop;
            }
        }
        if ('E' == buffer.charAt(buffer.length() - 1)) {
            buffer.append('0');
        }
        return new Double(buffer.toString());
    }

    /**
     * Converts date string to Date object use given format pattern.
     *
     * @param text   the date string
     * @param format the format pattern
     * @return the date
     */
    public static Date asDate(final String text, final String format) {
        try {
            return new SimpleDateFormat(format).parse(text);
        } catch (final ParseException e) {
            throw newClassCastException(text, Date.class);
        }
    }


    /**
     * Converts given object to Date.
     * <p>
     * The following formats are supported:
     * <ul>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy-MM-dd HH:mm:ss</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</li>
     * <li>EEE, dd MMM yyyy HH:mm:ss zzz</li>
     * </ul>
     *
     * @param obj the object
     * @return
     */
    public static Date asDate(final Object obj) {
        if (null == obj || obj instanceof Date) {
            return (Date) obj;
        }
        if (obj instanceof Calendar) {
            return ((Calendar) obj).getTime();
        }

        long timestamp = 0;
        if (obj instanceof String) {
            String text = (String) obj;

            final int len = text.length();
            if (0 == len) {
                return null;
            }

            // 优先尝试解析日期字符串
            if (-1 < text.indexOf('-')) {
                if (len <= DATE_FORMAT_PLAIN.length() && -1 == text.indexOf('T')) {
                    // yyyy-MM-dd HH:mm:ss.SSS,  may be missing millis or time.
                    final char c = text.charAt(len - 3);
                    final String finalText = ':' == c ? text + ".000" : ('-' == c ? text + " 00:00:00.000" : text);

                    try {
                        return new SimpleDateFormat(DATE_FORMAT_PLAIN).parse(finalText);
                    } catch (final ParseException ignore) {
                        // ignore
                    }
                } else if (len <= DATE_FORMAT_ISO8601.length() && -1 < text.indexOf('T')) {
                    // yyyy-MM-dd'T'HH:mm:ss.SSS'Z'/yyyy-MM-dd'T'HH:mm:ss.SSS+0800
                    final char c = text.charAt(len - 1);

                    if ('Z' == c) {
                        // Z 结尾 ISO8601, 应该是2012-12-12T00:00:00.000Z

                        // 缺失毫秒, eg: 2012-12-12T00:00:00Z
                        if (':' == text.charAt(len - 4)) {
                            final StringBuilder buf = new StringBuilder(text);
                            buf.insert(len - 1, ".000");
                            text = buf.toString();
                        }

                        try {
                            return new SimpleDateFormat(DATE_FORMAT_ISO8601_Z).parse(text);
                        } catch (ParseException ignore) {
                        }
                    } else {
                        // 应该为具体 TimeZone 2012-12-12T00:00:00.000+0800
                        if (hasTimeZone(text)) {
                            char ch = text.charAt(len - 3);

                            if (':' == ch) {
                                // +hh:mm 删除时区中可选的:
                                text = new StringBuilder(text).deleteCharAt(len - 3).toString();
                            } else if ('+' == ch || '-' == ch) {
                                // +hh -hh 填充缺失的分钟
                                text += "00";
                            }

                            if (Character.isDigit(text.charAt(text.length() - 9))) {
                                // 2012-02-07T12:00:00+0800 缺失毫秒
                                text = new StringBuilder(text).insert(text.length() - 5, ".000").toString();
                            }

                            try {
                                return new SimpleDateFormat(DATE_FORMAT_ISO8601).parse(text);
                            } catch (ParseException ignore) {
                            }
                        } else {
                            // 没有时区
                            // 2014-02-12T02:00:00
                            StringBuilder sb = new StringBuilder(text);
                            // 02:00:00
                            int timeLen = len - text.lastIndexOf('T') - 1;
                            if (timeLen <= 8) {
                                sb.append(".000");
                            }
                            sb.append('Z');
                            text = sb.toString();

                            try {
                                return new SimpleDateFormat(DATE_FORMAT_ISO8601_Z).parse(text);
                            } catch (ParseException ignore) {
                            }
                        }
                    }
                }
            } else {
                try {
                    return new SimpleDateFormat(DATE_FORMAT_RFC1123).parse(text);
                } catch (final ParseException ignore) {
                    //
                }
            }

            // 尝试解析为数字
            try {
                timestamp = Long.parseLong(text);
            } catch (final NumberFormatException ignore) {
                // ignore.
            }
        }

        if (obj instanceof Number) {
            timestamp = ((Number) obj).longValue();
        }

        if (timestamp <= 0) {
            throw newClassCastException(obj, Date.class);
        }

        return new Date(timestamp);
    }

    /**
     * 判断给定的日期字符串中是否包含时区
     */
    private static boolean hasTimeZone(final String date) {
        int len = date.length();
        if (len >= 6) {
            // +hh:mm
            char c = date.charAt(len - 6);
            if ('+' == c || '-' == c) {
                return true;
            }

            // +hhmm
            c = date.charAt(len - 5);
            if ('+' == c || '-' == c) {
                return true;
            }

            c = date.charAt(len - 3);
            if ('+' == c || '-' == c) {
                return true;
            }
        }
        return false;
    }

    private static ClassCastException newClassCastException(Object obj, Class<?> target) {
        throw new ClassCastException((obj != null ? obj.getClass().getName() : null) + '(' + obj + ')' + " cannot be cast to " + target.getName());
    }

}
