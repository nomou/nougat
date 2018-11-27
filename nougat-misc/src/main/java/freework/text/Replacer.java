package freework.text;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * 字符串替换工具类
 *
 * @author vacoor
 */
public abstract class Replacer {
    /**
     * 替换直到不改变
     */
    public static final int REPEAT_UNTIL_UNCHANGED = 0x01;
    /**
     * 忽略大小写
     */
    public static final int CASE_INSENSITIVE = 0x02;

    protected final boolean repeatUntilUnchanged; // 是否替换直到不改变
    protected final boolean ignoreCase;           // 是否忽略大小写

    protected Replacer(int flags) {
        this.repeatUntilUnchanged = 0 < (REPEAT_UNTIL_UNCHANGED & flags);
        this.ignoreCase = 0 < (CASE_INSENSITIVE & flags);
    }

    /**
     * 在给定的字符串上执行替换操作
     *
     * @param text 要替换的字符串
     * @return 替换后的字符串
     */
    public final String replace(String text) {
        if (repeatUntilUnchanged) {
            String replaced = text;
            String old = "";
            while (!old.equals(replaced)) {
                old = replaced;
                replaced = doInternalReplace(replaced);
            }
            return replaced;
        }
        return doInternalReplace(text);
    }

    protected abstract String doInternalReplace(String text);


    /**
     * 创建一个字符串匹配 {@link Replacer}
     *
     * @param from 匹配的字符串
     * @param to   替换的字符串
     * @return {@link Replacer}
     */
    public static Replacer create(final String from, final String to) {
        return create(from, to, 0);
    }

    /**
     * 创建一个字符串匹配 {@link Replacer}
     *
     * @param from  匹配的字符串
     * @param to    替换的字符串
     * @param flags 匹配标志
     * @return {@link Replacer}
     */
    public static Replacer create(final String from, final String to, final int flags) {
        return create(new String[]{from}, new String[]{to}, flags);
    }

    /**
     * 创建一个字符串匹配 {@link Replacer}
     *
     * @param from 匹配的字符串
     * @param to   替换的字符串
     * @return {@link Replacer}
     */
    public static Replacer create(final String[] from, final String[] to) {
        return create(from, to, 0);
    }

    /**
     * 创建一个字符串匹配 {@link Replacer}
     *
     * @param from  匹配的字符串
     * @param to    替换的字符串
     * @param flags 匹配标志
     * @return {@link Replacer}
     */
    public static Replacer create(final String[] from, final String[] to, final int flags) {
        return new Replacer(flags) {
            @Override
            protected String doInternalReplace(String text) {
                return Replacer.replace(text, from, to, ignoreCase, repeatUntilUnchanged, 99);
            }

            @Override
            public String toString() {
                return "Replacer{" + "from='" + Arrays.toString(from) + '\'' + ", to='" + Arrays.toString(to) + '\'' + '}';
            }
        };
    }

    /**
     * 创建一个使用给定正则模式进行匹配替换的 {@link Replacer}
     *
     * @param pattern 匹配模式
     * @param to      替换字符串
     * @return {@link Replacer}
     */
    public static Replacer create(final Pattern pattern, final String to) {
        return create(pattern, to, 0);
    }

    /**
     * 创建一个使用给定正则模式进行匹配替换的 {@link Replacer}
     *
     * @param pattern 匹配模式
     * @param to      替换字符串
     * @param flags   匹配标志
     * @return {@link Replacer}
     */
    public static Replacer create(final Pattern pattern, final String to, final int flags) {
        final boolean caseInsensitive = 0 == (CASE_INSENSITIVE & flags);
        final int patternFlags = pattern.flags();
        final boolean patternCaseInsensitive = (0 != (patternFlags & Pattern.CASE_INSENSITIVE));

        final Pattern finalPattern = (caseInsensitive != patternCaseInsensitive)
                ? Pattern.compile(pattern.pattern(), caseInsensitive ? patternFlags & Pattern.CASE_INSENSITIVE : patternFlags | Pattern.CASE_INSENSITIVE)
                : pattern;

        return new Replacer(flags) {
            @Override
            public String doInternalReplace(String text) {
                return finalPattern.matcher(text).replaceAll(to);
            }

            @Override
            public String toString() {
                return "Replacer{" + "from='" + finalPattern.pattern() + '\'' + ", to='" + to + '\'' + '}';
            }
        };
    }


    /**
     * <p>
     * Replaces all occurrences of Strings within another String.
     * </p>
     * <p>
     * <p>
     * A <code>null</code> reference passed to this method is a no-op, or if
     * any "search string" or "string to replace" is null, that replace will be
     * ignored.
     * </p>
     * <p/>
     * <pre>
     *  replace(null, *, *, *) = null
     *  replace("", *, *, *) = ""
     *  replace("aba", null, null, *) = "aba"
     *  replace("aba", new String[0], null, *) = "aba"
     *  replace("aba", null, new String[0], *) = "aba"
     *  replace("aba", new String[]{"a"}, null, *) = "aba"
     *  replace("aba", new String[]{"a"}, new String[]{""}, *) = "b"
     *  replace("aba", new String[]{null}, new String[]{"a"}, *) = "aba"
     *  replace("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}, *) = "wcte"
     *  (example of how it repeats)
     *  replace("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, false) = "dcte"
     *  replace("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, true) = "tcte"
     *  replace("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}, *) = IllegalArgumentException
     * </pre>
     *
     * @param text       text to search and replace in, no-op if null
     * @param from       the Strings to search for, no-op if null
     * @param to         the Strings to replace them with, no-op if null
     * @param repeat     if true, then replace repeatedly
     *                   until there are no more possible replacements or timeToLive < 0
     * @param timeToLive if less than 0 then there is a circular reference and endless
     *                   loop
     * @return the text with any replacements processed, <code>null</code> if
     * null String input
     * @throws IllegalArgumentException  if the search is repeating and there is an endless loop due
     *                                   to outputs of one being inputs to another
     * @throws IndexOutOfBoundsException if the lengths of the arrays are not the same (null is ok,
     *                                   and/or size 0)
     * @since 2.4
     */
    private static String replace(String text, String[] from, String[] to, boolean ignoreCase, boolean repeat, int timeToLive) {

        // mchyzer Performance note: This creates very few new objects (one major goal)
        // let me know if there are performance requests, we can create a harness to measure

        if (text == null || text.length() == 0 || from == null ||
                from.length == 0 || to == null || to.length == 0) {
            return text;
        }

        // if recursing, this shouldnt be less than 0
        if (timeToLive < 0) {
            throw new IllegalStateException("TimeToLive of " + timeToLive + " is less than 0: " + text);
        }

        int searchLength = from.length;
        int replacementLength = to.length;

        // make sure lengths are ok, these need to be equal
        if (searchLength != replacementLength) {
            throw new IllegalArgumentException("Search and Replace array lengths don't match: "
                    + searchLength
                    + " vs "
                    + replacementLength);
        }

        // keep track of which still have matches
        boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

        // index on index that the match was found
        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex = -1;

        // index of replace array that will replace the search string found
        // NOTE: logic duplicated below START
        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i] || from[i] == null || from[i].length() == 0 || to[i] == null) {
                continue;
            }
            tempIndex = indexOfIgnoreCase(text, from[i], 0, ignoreCase);

            // see if we need to keep searching for this
            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            } else {
                if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }
        }
        // NOTE: logic mostly below END

        // no search strings found, we are done
        if (textIndex == -1) {
            return text;
        }

        int start = 0;

        // get a good guess on the size of the result buffer so it doesnt have to double if it goes over a bit
        int increase = 0;

        // count the replacement text elements that are larger than their corresponding text being replaced
        for (int i = 0; i < from.length; i++) {
            if (from[i] == null || to[i] == null) {
                continue;
            }
            int greater = to[i].length() - from[i].length();
            if (greater > 0) {
                increase += 3 * greater; // assume 3 matches
            }
        }
        // have upper-bound at 20% increase, then let Java take over
        increase = Math.min(increase, text.length() / 5);

        StringBuilder buf = new StringBuilder(text.length() + increase);

        while (textIndex != -1) {

            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(to[replaceIndex]);

            start = textIndex + from[replaceIndex].length();

            textIndex = -1;
            replaceIndex = -1;
            tempIndex = -1;
            // find the next earliest match
            // NOTE: logic mostly duplicated above START
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i] || from[i] == null ||
                        from[i].length() == 0 || to[i] == null) {
                    continue;
                }
                tempIndex = indexOfIgnoreCase(text, from[i], start, ignoreCase);

                // see if we need to keep searching for this
                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else {
                    if (textIndex == -1 || tempIndex < textIndex) {
                        textIndex = tempIndex;
                        replaceIndex = i;
                    }
                }
            }
            // NOTE: logic duplicated above END

        }
        int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        String result = buf.toString();
        if (!repeat) {
            return result;
        }

        return replace(result, from, to, ignoreCase, true, timeToLive - 1);
    }

    private static int indexOfIgnoreCase(String str, String searchStr, int startPos, boolean ignoreCase) {
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
}
