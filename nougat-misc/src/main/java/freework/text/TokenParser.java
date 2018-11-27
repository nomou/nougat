/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.text;

/**
 * Token 解析器
 * <p/>
 * <code>
 * <pre>
 * String parsed = new TokenParser("${", "}") {
 *    // Override
 *    protected String handleToken(String content) {
 *       return content;
 *    }
 * }.doParse("hehe ${x} ");
 * </pre>
 * </code>
 *
 * @author vacoor
 */
public abstract class TokenParser {
    private final String openToken;
    private final String closeToken;

    protected TokenParser(String openToken, String closeToken) {
        if (null == openToken || null == closeToken || 1 > openToken.length() || 1 > closeToken.length()) {
            throw new IllegalArgumentException("open token and close token must be not null");
        }

        this.openToken = openToken;
        this.closeToken = closeToken;
    }

    /**
     * 解析给定的文字
     *
     * @param text 要解析的文本
     * @return 解析后的文本
     */
    public final String doParse(String text) {
        StringBuilder buff = new StringBuilder();
        if (text != null && text.length() > 0) {
            char[] chars = text.toCharArray();
            int offset = 0;
            int start = text.indexOf(openToken, offset);
            while (start > -1) {
                if (start > 0 && chars[start - 1] == '\\') {
                    // the variable is escaped. remove the backslash.
                    buff.append(chars, offset, start - 1).append(openToken);
                    offset = start + openToken.length();
                } else {
                    int end = text.indexOf(closeToken, start);
                    if (end == -1) {
                        buff.append(chars, offset, chars.length - offset);
                        offset = chars.length;
                    } else {
                        buff.append(chars, offset, start - offset);
                        offset = start + openToken.length();
                        String content = new String(chars, offset, end - offset);
                        buff.append(handleToken(content));
                        offset = end + closeToken.length();
                    }
                }
                start = text.indexOf(openToken, offset);
            }
            if (offset < chars.length) {
                buff.append(chars, offset, chars.length - offset);
            }
        }
        return buff.toString();
    }

    /**
     * 解析给定的Token文本
     *
     * @param content Token 名称
     * @return Token 解析后的值
     */
    protected abstract String handleToken(String content);
}
