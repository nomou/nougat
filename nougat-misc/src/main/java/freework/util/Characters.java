/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.util.Locale;

/**
 * 字符编码转换
 * 主要用于将字符串转换为Unicode字符串来传输, US-ASCII 不转换, 对于特殊字符 \r \n 会转换为 "\r\n" 形式
 * <p>
 * 网络传输中编码设置一旦忘记或错误就必然的乱码
 * 在与支持 Unicode编码的系统交互,eg: js, 可以使用 Unicode 来解决这个问题，编码设置错误/不设置也不会有乱码
 *
 * @author Vacoor
 */
public abstract class Characters {
    private static final int ESCAPE_STANDARD = -1;
    private static final int ESCAPE_NONE = 0;

    // ASCII 转义表: -1 -- 需要 Unicode 转换, 0 -- 不需要, 否则为特殊转义eg: \r\n
    private static final int[] ASCII_ESCAPE_TABLE = new int[128];

    static {
        // ASCII < 32 的不可见字符置为标准转义
        for (int i = 0; i < 32; i++) {
            ASCII_ESCAPE_TABLE[i] = ESCAPE_STANDARD;
        }
        // 修正需要特殊转义的字符
        ASCII_ESCAPE_TABLE['\b'] = 'b';  // 回退
        ASCII_ESCAPE_TABLE['\t'] = 't';  // Tab
        ASCII_ESCAPE_TABLE['\n'] = 'n';  // 换行
        ASCII_ESCAPE_TABLE['\f'] = 'f';  // 换页
        ASCII_ESCAPE_TABLE['\r'] = 'r';  // 回车
        ASCII_ESCAPE_TABLE['\\'] = '\\'; // 转义
    }

    /**
     * 是否是 ASCII 字符
     *
     * @param c
     * @return
     */
    public static boolean isAscii(char c) {
        return c >= 0x0 && c < 0x80;
    }

    /**
     * 是否是 ASCII 不可见字符
     *
     * @param c
     * @return
     */
    public static boolean isInvisibleAscii(char c) {
        return c >= 0 && c < 0x20;
    }

    /*-
       JS 参考实现:
       String.prototype.toUnicodeString = function () {
          var unicode = "";
          for (var i = 0; i < this.length; i++) {
             var c = this.charCodeAt(i).toString(16).toUpperCase();
             unicode += "\\u" + '0000'.substring(0, 4 - c.length) + c;
          }
          return unicode;
       }
     */
    public static String toUnicode(CharSequence charseq) {
        StringBuilder frag = new StringBuilder();
        for (int i = 0; i < charseq.length(); i++) {
            char c = charseq.charAt(i);

            // ASCII < 128
            if (c < ASCII_ESCAPE_TABLE.length) {
                int escape = ASCII_ESCAPE_TABLE[c];

                if (ESCAPE_NONE == escape) {    // 不需要转义
                    frag.append(c);
                    continue;
                } else if (ESCAPE_STANDARD != escape) {    // 需要转义但不需要准转义(特殊转义)
                    frag.append("\\").append((char) escape);
                    continue;
                }
            }

            // 标准转义
            /*
            String hex = Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
            frag.append("\\u").append("0000".substring(0, 4 - hex.length())).append(hex);
            */
            if (c > 0xFFF) {
                frag.append("\\u").append(Integer.toHexString(c).toUpperCase(Locale.ENGLISH));
            } else if (c > 0xFF) {
                frag.append("\\u0").append(Integer.toHexString(c).toUpperCase(Locale.ENGLISH));
            } else if (c > 0xF) {
                frag.append("\\u00").append(Integer.toHexString(c).toUpperCase(Locale.ENGLISH));
            } else {
                frag.append("\\u000").append(Integer.toHexString(c).toUpperCase(Locale.ENGLISH));
            }
        }
        return frag.toString();
    }

    private Characters() {
    }
}
