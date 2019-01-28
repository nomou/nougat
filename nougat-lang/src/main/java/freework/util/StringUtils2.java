/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.text.StringCharacterIterator;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Utilities of String.
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public abstract class StringUtils2 {
    private static final String NONE = "";
    private static final char SPACE = ' ';
    private static final char UNDERSCORE = '_';

    /**
     * Non-instantiate.
     */
    private StringUtils2() {
    }

    /**
     * Check that the given CharSequence is neither {@code null} nor of length 0.
     * <br>Note: Will return {@code true} for a CharSequence that purely consists of whitespace.
     *
     * @param text the CharSequence to check (may be {@code null})
     * @return {@code true} if the CharSequence is not null and has length
     */
    public static boolean hasLength(final CharSequence text) {
        return text != null && 0 < text.length();
    }

    /**
     * Check whether the given CharSequence has actual text.
     * More specifically, returns {@code true} if the string not {@code null},
     * its length is greater than 0, and it contains at least one non-whitespace character.
     *
     * @param text the CharSequence to check (may be {@code null})
     * @return {@code true} if the CharSequence is not {@code null},
     * its length is greater than 0, and it does not contain whitespace only
     */
    public static boolean hasText(final CharSequence text) {
        int len;
        if (text == null || (len = text.length()) == 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new string that is a substring of this string, negative index indicate the offset of the end.
     * (-1 indicates the index of the last character).
     * <p>
     * Examples:
     * <blockquote><pre>
     *  slice("0123456789", 0, 1)   returns "0"
     *  slice("0123456789", 0, -1)  returns "012345678"
     *  slice("0123456789", -2, -1) returns "8"
     *  slice("0123456789", -2, 0)  returns "" (not throw exception)
     *  slice("0123456789", -1, 10) returns "9"
     * </pre></blockquote>
     *
     * @param text       the string
     * @param beginIndex the begin index
     * @param endIndex   the end index
     * @return the specified substring
     */
    public static String slice(final String text, int beginIndex, int endIndex) {
        if (!hasLength(text)) {
            return text;
        }
        final int len = text.length();
        beginIndex = beginIndex < 0 ? beginIndex + len : beginIndex;
        endIndex = endIndex < 0 ? endIndex + len : endIndex;
        return beginIndex > endIndex ? NONE : text.substring(beginIndex, endIndex);
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring.
     *
     * @param text       the string
     * @param search     the substring to search for
     * @param ignoreCase true if search should ignore case
     * @return the index of the first occurrence of the specified substring,
     * or -1 if there is no such occurrence.
     */
    public static int indexOf(final String text, final String search, final boolean ignoreCase) {
        return indexOf(text, search, 0, ignoreCase);
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring, starting at the specified index.
     *
     * @param text       the string
     * @param search     the substring to search for
     * @param fromIndex  the index from which to start the search
     * @param ignoreCase true if search should ignore case
     * @return the index of the first occurrence of the specified substring,
     * starting at the specified index, or -1 if there is no such occurrence.
     */
    public static int indexOf(final String text, final String search, int fromIndex, final boolean ignoreCase) {
        if (null == text || null == search) {
            return -1;
        }
        if (0 > fromIndex) {
            fromIndex = 0;
        }
        final int endLimit = (text.length() - search.length()) + 1;
        if (fromIndex > endLimit) {
            return -1;
        }
        if (0 == search.length()) {
            return fromIndex;
        }
        for (int i = fromIndex; i < endLimit; i++) {
            if (text.regionMatches(ignoreCase, i, search, 0, search.length())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Tests if the text starts with the specified prefix.
     *
     * @param text       the text
     * @param prefix     the prefix
     * @param ignoreCase ignoring case if true
     * @return true if the text starts with the specified prefix
     */
    public static boolean startsWith(final String text, final String prefix, final boolean ignoreCase) {
        if (null == text || null == prefix || text.length() < prefix.length()) {
            return false;
        }
        if (text.startsWith(prefix)) {
            return true;
        }
        return ignoreCase && text.substring(0, prefix.length()).toLowerCase().equals(prefix.toLowerCase());
    }

    /**
     * Tests if the text ends with the specified suffix.
     *
     * @param text       the text
     * @param suffix     the suffix
     * @param ignoreCase ignoring case if true
     * @return true if the text ends with the specified prefix
     */
    public static boolean endsWith(final String text, final String suffix, final boolean ignoreCase) {
        if (null == text || null == suffix || text.length() < suffix.length()) {
            return false;
        }
        if (text.endsWith(suffix)) {
            return true;
        }
        return (ignoreCase && text.substring(text.length() - suffix.length()).toLowerCase().equals(suffix.toLowerCase()));
    }

    /**
     * Removes control characters (char &lt;= 32) from both  ends of the string.
     *
     * @param text the string
     * @return the trimmed string
     */
    public static String trim(final String text) {
        return text == null ? null : text.trim();
    }

    /**
     * Removes control characters (char &lt;= 32) from the begin/end of the string.
     *
     * @param text the string
     * @param left true if should remove from begin, otherwise remove from end
     * @return the trimmed string
     */
    public static String trim(final String text, final boolean left) {
        if (!hasLength(text)) {
            return text;
        }
        int st = 0;
        int len = text.length();
        if (left) {
            while (st < len && text.charAt(st) <= SPACE) {
                st++;
            }
            return 0 < st ? text.substring(st, len) : text;
        } else {
            while (st < len && text.charAt(len - 1) <= SPACE) {
                len--;
            }
            return len < text.length() ? text.substring(st, len) : text;
        }
    }

    /**
     * Removes control characters (char &lt;= 32) from both ends of the string,
     * returning {@code null} if the String is empty ("") after the trim or if it is {@code null}.
     *
     * @param text the string
     * @return the trimmed string, {@code null} if only chars &lt;= 32, empty or null string input
     */
    public static String trimToNull(final String text) {
        final String trim = trim(text);
        return !hasLength(trim) ? null : trim;
    }

    /**
     * Removes control characters (char &lt;= 32) from both ends of this String returning an empty string (""),
     * if the string is empty ("") after the trim or if it is {@code null}.
     *
     * @param text the string
     * @return the trimmed string, or an empty string if {@code null} input
     */
    public static String trimToEmpty(final String text) {
        final String trim = trim(text);
        return trim == null ? NONE : trim;
    }

    /**
     * Pad a String with a specified character.
     *
     * @param text    the string to pad out
     * @param padChar the character to pad with
     * @param size    the size to pad to
     * @param leftPad true if left pad, otherwise right pad
     * @return original string if no padding is necessary, otherwise padded string
     */
    public static String pad(final String text, final char padChar, int size, final boolean leftPad) {
        final int len = text.length();
        if (len >= size) {
            return text;
        }
        final StringBuilder buff = new StringBuilder();
        for (int i = 0; i < size - len; i++) {
            buff.append(padChar);
        }
        if (leftPad) {
            buff.append(text);
        } else {
            buff.insert(0, text);
        }
        return buff.toString();
    }

    /**
     * Capitalizes a String changing the first letter to title case as per {@link Character#toTitleCase(char)}.
     * No other letters are changed.
     *
     * @param text the string to capitalize
     * @return the capitalized string
     */
    public static String capitalize(final String text) {
        if (!hasLength(text) || Character.isTitleCase(text.charAt(0))) {
            return text;
        }
        final char[] chars = text.toCharArray();
        chars[0] = Character.toTitleCase(chars[0]);
        return new String(chars);
    }

    /**
     * Uncapitalizes a String changing the first letter to title case as per {@link Character#toLowerCase(char)}.
     * No other letters are changed.
     *
     * @param text the String to uncapitalize
     * @return the uncapitalized string
     */
    public static String uncapitalize(final String text) {
        if (!hasLength(text) || Character.isLowerCase(text.charAt(0))) {
            return text;
        }
        if (text.length() > 1 && Character.isTitleCase(text.charAt(0)) && Character.isTitleCase(text.charAt(1))) {
            return text;
        }
        final char[] chars = text.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * Converts underscore-style string into CamelCase-style.
     *
     * @param text               the underscore-style string
     * @param firstCharUpperCase true if first char should uppercase
     * @return the CamelCase-style string
     */
    public static String underscoreToCamelCase(final String text, final boolean firstCharUpperCase) {
        return delimitedToCamelCase(text, UNDERSCORE, firstCharUpperCase);
    }

    /**
     * Converts CamelCase-style string into underscore-style.
     *
     * @param text      the CamelCase-style string
     * @param upperCase true if should uppercase
     * @return the underscore-style string
     */
    public static String camelCaseToUnderscore(final String text, final boolean upperCase) {
        return camelCaseToDelimited(text, UNDERSCORE, upperCase);
    }

    /**
     * Converts delimited-style string into CamelCase-style.
     *
     * @param text               the underscore-style string
     * @param delimiter          the delimiter
     * @param firstCharUpperCase true if first char should uppercase
     * @return the CamelCase-style string
     */
    @SuppressWarnings("PMD.AvoidComplexConditionRule")
    public static String delimitedToCamelCase(final String text, final char delimiter, final boolean firstCharUpperCase) {
        if (!hasLength(text)) {
            return text;
        }
        final StringCharacterIterator it = new StringCharacterIterator(text.toLowerCase(Locale.ENGLISH));
        final StringBuilder segment = new StringBuilder(text.length());
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
     * Converts CamelCase-style string into delimited-style.
     *
     * @param text      the CamelCase-style string
     * @param delimiter the delimiter
     * @param upperCase true if should uppercase
     * @return the delimited-style string
     */
    public static String camelCaseToDelimited(final String text, final char delimiter, final boolean upperCase) {
        if (!hasLength(text)) {
            return text;
        }
        final char[] chars = text.toCharArray();
        final StringBuilder segment = new StringBuilder();
        segment.append(upperCase ? Character.toUpperCase(chars[0]) : Character.toLowerCase(chars[0]));

        for (int i = 1; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c) && i + 1 < chars.length && !Character.isUpperCase(chars[i + 1]) && delimiter != chars[i + 1]) {
                segment.append(delimiter);
            }
            c = upperCase ? Character.toUpperCase(c) : Character.toLowerCase(c);
            segment.append(c);
        }
        return segment.toString();
    }

    /**
     * Breaks the specified string into array.
     *
     * @param text       the string
     * @param delimiters the delimiters
     * @return the array
     */
    @SuppressWarnings("unchecked")
    public static String[] tokenizeToArray(final String text, final String delimiters) {
        final Iterable<String> it = tokenize(text, delimiters);
        return Arrays2.create(String.class, it);
    }

    /**
     * Breaks the specified string into iterable object using {@code StringTokenizer} default delimiters(" \t\n\r\f").
     *
     * @param text the string
     * @return the iterable
     */
    public static Iterable<String> tokenize(final String text) {
        return tokenize(new StringTokenizer(text));
    }

    /**
     * Breaks the specified string into iterable object.
     *
     * @param text       the string
     * @param delimiters the delimiters
     * @return the iterable
     */
    public static Iterable<String> tokenize(final String text, final String delimiters) {
        return tokenize(new StringTokenizer(text, delimiters));
    }

    /**
     * Converts the tokenizer to iterable object.
     *
     * @param tokenizer the tokenizer
     * @return the iterable
     */
    public static Iterable<String> tokenize(final StringTokenizer tokenizer) {
        return new Iterable<String>() {
            /**
             * {@inheritDoc}
             */
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean hasNext() {
                        return tokenizer.hasMoreTokens();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public String next() {
                        return tokenizer.nextToken();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
