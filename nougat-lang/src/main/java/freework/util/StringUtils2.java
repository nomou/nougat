/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * 字符串工具类
 *
 * @author vacoor
 */
public abstract class StringUtils2 {
    public static final String NONE = "";

    /**
     * 给定字符序列是否有长度
     *
     * @param charseq
     * @return
     */
    public static boolean hasLength(CharSequence charseq) {
        return charseq != null && charseq.length() > 0;
    }

    /**
     * 给定字符序列是否包含非空白字符
     *
     * @param charseq
     * @return
     */
    public static boolean hasText(CharSequence charseq) {
        int len;
        if (charseq == null || (len = charseq.length()) == 0) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(charseq.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 允许负数索引 (最后一个元素索引为 -1)
     *
     * @param str
     * @param start
     * @param end
     * @return
     */
    public static String slice(String str, int start, int end) {
        if (!hasLength(str)) {
            return str;
        }

        int len = str.length();
        start = start < 0 ? start + len : start;
        end = end < 0 ? end + len : end;
        return start > end ? NONE : str.substring(start, end);
    }


    /**
     * 首字母大写
     *
     * @param str
     * @return
     */
    public static String capitalize(String str) {
        if (!hasLength(str)) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * 取消首字母大写
     * 如果前两个字符都是大写字母, 则不转换
     */
    public static String uncapitalize(String str) {
        if (!hasLength(str)) {
            return str;
        }

        // 如果前两个字符都是大写则不转换
        if (str.length() > 1 && Character.isUpperCase(str.charAt(0)) && Character.isUpperCase(str.charAt(1))) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * 反转大小写
     *
     * @param str
     * @return
     */
    public String swapCase(String str) {
        if (hasLength(str)) {
            return str;
        }

        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isLowerCase(c)) {
                chars[i] = Character.toUpperCase(c);
            } else if (Character.isUpperCase(c)) {
                chars[i] = Character.toLowerCase(c);
            } else if (Character.isTitleCase(c)) {
                chars[i] = Character.toLowerCase(c);
            }
        }
        return new String(chars);
    }


    public static String trim(String str) {
        return str == null ? null : str.trim();
    }


    /**
     * left trim
     *
     * @param str
     * @return
     */
    public static String ltrim(String str) {
        if (!hasLength(str)) {
            return str;
        }

        int st = 0;
        final int len = str.length();
        for (; st < len && str.charAt(st) <= ' '; st++) ;
        return st > 0 ? str.substring(st, len) : str;
    }

    public static String rtrim(String str) {
        if (!hasLength(str)) {
            return str;
        }

        final int st = 0;
        int len = str.length();
        for (; st < len && str.charAt(len - 1) <= ' '; len--) ;
        return len < str.length() ? str.substring(st, len) : str;
    }

    public static String pad(String str, char padChar, int total, boolean lpad) {
        int len = str.length();
        if (len >= total) {
            return str;
        }

        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < total - len; i++) {
            buff.append(padChar);
        }

        if (lpad) {
            buff.append(str);
        } else {
            buff.insert(0, str);
        }
        return buff.toString();
    }

    public static String trimToNull(String str) {
        final String trim = trim(str);
        return !hasLength(trim) ? null : trim;
    }

    public static String trimToEmpty(String str) {
        final String trim = trim(str);
        return trim == null ? NONE : trim;
    }


    /**
     * 下划线写法 --&gt; 驼峰写法
     *
     * @param str
     * @param firstCharUpperCase 是否首字母大写
     * @return
     */
    public static String underscoreToCamelCase(String str, boolean firstCharUpperCase) {
        return delimitedToCamelCase(str, '_', firstCharUpperCase);
    }

    /**
     * 特定分隔符 --&gt; 驼峰写法
     *
     * @param str
     * @param delimiter
     * @param firstCharUpperCase
     * @return
     */
    public static String delimitedToCamelCase(String str, char delimiter, boolean firstCharUpperCase) {
        if (!hasLength(str)) {
            return str;
        }

        StringCharacterIterator it = new StringCharacterIterator(str.toLowerCase(Locale.ENGLISH));
        StringBuilder segment = new StringBuilder(str.length());
        boolean undlerline = false;
        for (; it.getIndex() < it.getEndIndex(); it.next()) {
            char c = it.current();
            if ((0 == it.getIndex() && firstCharUpperCase) || undlerline) {
                segment.append(Character.toUpperCase(c));
                undlerline = false;
            } else if (delimiter == c) {
                undlerline = true;
            } else {
                segment.append(c);
            }
        }

        return segment.toString();
    }

    /**
     * 驼峰写法 --&gt; 下划线写法
     *
     * @param str
     * @return
     */
    public static String camelCaseToUnderscore(String str, boolean upperCase) {
        if (!hasLength(str)) {
            return str;
        }

        char[] chars = str.toCharArray();
        StringBuilder segment = new StringBuilder();
        segment.append(upperCase ? Character.toUpperCase(chars[0]) : Character.toLowerCase(chars[0]));

        for (int i = 1; i < chars.length; i++) {
            char c = chars[i];
            // 大写字母后不是大写字母且不是下划线则追加
            if (Character.isUpperCase(c) && i + 1 < chars.length && !Character.isUpperCase(chars[i + 1]) && '_' != chars[i + 1]) {
                segment.append('_');
            }
            c = upperCase ? Character.toUpperCase(c) : Character.toLowerCase(c);
            segment.append(c);
        }

        return segment.toString();
    }
    /*
    public static String unqualifiedClassName(Class type) {
        if (type.isArray()) {
            return unqualifiedClassName(type.getComponentType())+"Array";
        }
        String name = type.getName();
        return name.substring(name.lastIndexOf('.')+1);
    }
    */

    public static String clean(String in) {
        String out = in;

        if (in != null) {
            out = in.trim();
            if (out.equals(NONE)) {
                out = null;
            }
        }

        return out;
    }

    /**
     * 清理字符串数组中 null / "", 始终返回一个字符串数组
     *
     * @param array
     * @return
     */
    public static String[] clean(String[] array) {
        if (array == null) {
            return new String[0];
        }
        List<String> result = new ArrayList<String>(array.length);
        for (String s : array) {
            if (null != clean(s)) {
                result.add(s);
            }
        }
        return result.toArray(new String[result.size()]);
    }


    public static <T> String join(T[] array, String separator) {
        return Arrays2.toString(array, separator);
    }

    public static boolean startsWith(String str, String prefix) {
        return startsWith(str, prefix, false);
    }

    /**
     * Test if the given String starts with the specified prefix,
     * ignoring upper/lower case.
     * <p>
     * <p>Copied from the Spring Framework while retaining all license, copyright and author information.
     *
     * @param str    the String to check
     * @param prefix the prefix to look for
     * @return <code>true</code> starts with the specified prefix (ignoring case), <code>false</code> if it does not.
     * @see java.lang.String#startsWith
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return startsWith(str, prefix, true);
    }

    public static boolean endsWith(String str, String suffix) {
        return endsWith(str, suffix, false);
    }

    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return endsWith(str, suffix, true);
    }

    public static boolean startsWith(String str, String prefix, boolean ignoreCase) {
        if (str == null || prefix == null || str.length() < prefix.length()) {
            return false;
        }
        if (str.startsWith(prefix)) {
            return true;
        }
        return ignoreCase && str.substring(0, prefix.length()).toLowerCase().equals(prefix.toLowerCase());
    }

    public static boolean endsWith(String str, String suffix, boolean ignoreCase) {
        if (null == str || null == suffix || str.length() < suffix.length()) {
            return false;
        }
        if (str.endsWith(suffix)) {
            return true;
        }
        return (ignoreCase && str.substring(str.length() - suffix.length()).toLowerCase().equals(suffix.toLowerCase()));
    }

    public static int indexOfIgnoreCase(String text, String search, boolean ignoreCase) {
        return indexOfIgnoreCase(text, search, 0, ignoreCase);
    }

    public static int indexOfIgnoreCase(String str, String searchStr, int startPos, boolean ignoreCase) {
        if (str == null || searchStr == null) {
            return -1;
        }
        if (startPos < 0) {
            startPos = 0;
        }
        int endLimit = (str.length() - searchStr.length()) + 1;
        if (startPos > endLimit) {
            return -1;
        }
        if (searchStr.length() == 0) {
            return startPos;
        }
        for (int i = startPos; i < endLimit; i++) {
            if (str.regionMatches(ignoreCase, i, searchStr, 0, searchStr.length())) {
                return i;
            }
        }
        return -1;
    }

    public static String[] tokenizeToStringArray(String text, String separators) {
        Iterable<String> it = tokenize(text, separators);
        return Arrays2.asArray(it, String.class);
    }

    public static Iterable<String> tokenize(String s) {
        return tokenize(new StringTokenizer(s));
    }

    public static Iterable<String> tokenize(String s, String separators) {
        final StringTokenizer tokenizer = new StringTokenizer(s, separators);
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return tokenizer.hasMoreTokens();
                    }

                    @Override
                    public String next() {
                        return tokenizer.nextToken();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public static Iterable<String> tokenize(final StringTokenizer tokenizer) {
        return new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return tokenizer.hasMoreTokens();
                    }

                    @Override
                    public String next() {
                        return tokenizer.nextToken();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    private StringUtils2() {
    }
}
