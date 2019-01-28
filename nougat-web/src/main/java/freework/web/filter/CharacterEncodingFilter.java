package freework.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 字符编码设置过滤器.
 * <p/>
 * 额外支持不依赖于容器的 get 参数的编码处理, 对于没有设置 get 参数藏石自动检测 GET 请求参数编码并解码, 不支持 POST URL 中带 GET 参数.
 * eg: POST xxx?name=23
 * <p/>
 * 注:<br>
 * Chrome, FF, Safari GET 请求均使用 UTF-8 编码进行 URL 编码后提交,
 * IE GET 直接输入中文请求会直接使用不编码的GBK字符, 通过 form 请求会使用 GBK 编码进行 URL 编码后提交,
 * (IE 测试为 7,8,9,10,11).
 *
 * @author vacoor
 * @since 1.0
 */
public class CharacterEncodingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterEncodingFilter.class);

    /**
     * HTTP GET.
     */
    private static final String GET = "GET";

    /**
     * UTF-8 charset.
     */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * GBK charset.
     */
    private static final Charset GBK = Charset.forName("GBK");

    /**
     * 待检测的字符集.
     */
    private static final Charset[] DETECT_CHARSETS = new Charset[]{UTF8, GBK};

    /**
     * 无法检测字符集时使用的字符集.
     */
    private static final Charset DEFAULT_FALLBACK_QUERY_ENCODING = UTF8;

    // private static final String CHAR = "✓";
    // private static String param = "utf8";

    /**
     * POST 请求使用的字符编码.
     */
    private String encoding;

    /**
     * POST 是否强制使用给定的字符编码.
     */
    private boolean forceEncoding;

    /**
     * GET 请求参数编码.
     */
    private String queryEncoding;

    /**
     * GET 参数编码无法检测时使用的字符编码.
     */
    private String fallbackQueryEncoding;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws ServletException {
        encoding = getInitParam("encoding");
        forceEncoding = Boolean.parseBoolean(getInitParam("forceEncoding"));
        queryEncoding = getInitParam("queryEncoding");
        fallbackQueryEncoding = getInitParam("fallbackQueryEncoding");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(final ServletRequest request, final ServletResponse response,
                                    final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            this.doFilterInternal((HttpServletRequest) request, (HttpServletResponse) response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * 执行内部过滤.
     *
     * @param httpRequest  Http Request
     * @param httpResponse Http Response
     * @param filterChain  Filter Chain
     * @throws IOException      IOException
     * @throws ServletException ServletException
     */
    protected void doFilterInternal(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
                                    final FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest finalRequest = httpRequest;
        if (GET.equals(finalRequest.getMethod())) {
            final String queryString = finalRequest.getQueryString();
            if (null != queryString) {
                final Charset[] charsets = null != queryEncoding
                        ? new Charset[]{Charset.forName(queryEncoding)}
                        : DETECT_CHARSETS;
                final Charset fallbackCharset = null != fallbackQueryEncoding
                        ? Charset.forName(fallbackQueryEncoding)
                        : DEFAULT_FALLBACK_QUERY_ENCODING;
                final Map<String, String[]> paramsMap = parseQueryString(queryString, charsets, fallbackCharset);
                finalRequest = 0 < paramsMap.size() ? new ParameterProxiedRequest(finalRequest, paramsMap) : finalRequest;
            }
        }

        final boolean applyEncoding = this.forceEncoding || null == finalRequest.getCharacterEncoding();
        if (null != this.encoding && applyEncoding) {
            finalRequest.setCharacterEncoding(this.encoding);
            if (this.forceEncoding) {
                httpResponse.setCharacterEncoding(this.encoding);
            }
        }

        filterChain.doFilter(finalRequest, httpResponse);
    }

    /**
     * 字符集次数统计.
     */
    private static class CharsetCounter {
        private final Charset charset;
        private int count = 0;

        private CharsetCounter(final Charset charset) {
            this.charset = charset;
        }
    }

    /**
     * 参数代理请求.
     */
    private class ParameterProxiedRequest extends HttpServletRequestWrapper {
        private final Map<String, String[]> paramsMap;

        public ParameterProxiedRequest(final HttpServletRequest httpRequest, final Map<String, String[]> paramsMap) {
            super(httpRequest);
            this.paramsMap = Collections.unmodifiableMap(paramsMap);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getParameterValues(final String name) {
            return paramsMap.get(name);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Enumeration getParameterNames() {
            return Collections.enumeration(paramsMap.keySet());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getParameter(final String name) {
            final String[] values = getParameterValues(name);
            return null != values && 0 < values.length ? values[0] : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<String, String[]> getParameterMap() {
            return paramsMap;
        }
    }

    /**
     * 使用给定的字符集来尝试解析 queryString (支持 IE form GET 提交参数, IE 地址栏直接输入提交参数, 其他浏览器 GET 参数).
     *
     * @param queryString    queryString
     * @param charsets       要尝试解析的字符集
     * @param defaultCharset 默认字符街
     * @return 参数
     */
    private static Map<String, String[]> parseQueryString(final String queryString,
                                                          final Charset[] charsets, final Charset defaultCharset) {
        String query = null != queryString ? queryString : "";
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("parse query string: {}", query);
        }

        final int start = query.indexOf("?");
        if (start == query.length() - 1) {
            return Collections.emptyMap();
        }
        query = query.substring(start + 1);

        final boolean urlEncoded = query.contains("%");
        final boolean unescape = !urlEncoded && hasNonAscii(query);
        final String[] params = query.split("&");

        /* 全是 ASCII 字符 */
        if (!urlEncoded && !unescape) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("no encoded or unescape char for query string: {}", query);
            }

            final Map<String, String[]> paramsMap = new HashMap<String, String[]>();
            final Map<String, List<String>> newParamsMap = new LinkedHashMap<String, List<String>>();
            for (final String param : params) {
                final String[] pair = param.split("=", 2);
                List<String> values = newParamsMap.get(pair[0]);
                if (null == values) {
                    values = new LinkedList<String>();
                    newParamsMap.put(pair[0], values);
                }
                values.add(1 < pair.length ? pair[1] : "");
            }

            for (final Map.Entry<String, List<String>> entry : newParamsMap.entrySet()) {
                final String name = entry.getKey();
                final List<String> values = entry.getValue();
                paramsMap.put(name, values.toArray(new String[values.size()]));
            }

            return paramsMap;
        } else {
            /* %转义, 或没转义 */
            final CharsetCounter[] counters = wrap(charsets);
            final Map<ByteBuffer, List<ByteBuffer>> paramsByteMap = urlEncoded
                    // IE form GET 提交使用 GBK 编码转义字符, 其他浏览器 GET 均使用 UTF_8 编码转义字符
                    ? doParseUrlEncodedQueryString(params, counters)
                    // IE 直接地址栏输入不会转义, 直接提交 GBK 编码字符
                    : doParseUnescapeQueryString(params, counters);

            // 按照使用频次排序
            Arrays.sort(counters, new Comparator<CharsetCounter>() {
                @Override
                public int compare(CharsetCounter o1, CharsetCounter o2) {
                    return o1.count > o2.count ? -1 : o1.count == o2.count ? 0 : 1;
                }
            });

            final Charset charset = 0 < counters.length && counters[0].count > 0 ? counters[0].charset : defaultCharset;

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("detected charset is {} for query string: {}", charset, queryString);
            }

            return decodeParamsMap(paramsByteMap, charset);
        }
    }

    /**
     * 获取给定文本是否包含非ASCII字符.
     *
     * @param text 待检测文本
     * @return 是否包含非ASCII字符
     */
    private static boolean hasNonAscii(final String text) {
        if (null == text) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 127) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据 URL 编码后的字符串解析为编码前的字节数组 (IE form GET 使用 GBK, 其他浏览器 GET 使用 UTF-8)
     *
     * @param params   URL 编码后的参数列表
     * @param counters 字符统计
     * @return 编码前的参数列表字节
     */
    private static Map<ByteBuffer, List<ByteBuffer>> doParseUrlEncodedQueryString(String[] params, CharsetCounter[] counters) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("parse url encoded params: {}", Arrays.toString(params));
        }

        Map<ByteBuffer, List<ByteBuffer>> paramsBytesMap = new LinkedHashMap<ByteBuffer, List<ByteBuffer>>();
        for (String param : params) {
            String[] pair = param.split("=", 2);
            byte[] nameBytes = decodeURIComponentToBytes(pair[0]);
            byte[] valueBytes = 1 < pair.length ? decodeURIComponentToBytes(pair[1]) : new byte[0];

            ByteBuffer key = ByteBuffer.wrap(nameBytes);
            List<ByteBuffer> values = paramsBytesMap.get(key);
            if (null == values) {
                paramsBytesMap.put(key, values = new LinkedList<ByteBuffer>());
            }
            values.add(ByteBuffer.wrap(valueBytes));

            CharsetCounter counter;
            // 如果Key被转义则记录探测到的编码
            if (pair[0].contains("%") && null != (counter = detect(nameBytes, counters))) {
                counter.count++;
            }

            // 如果值被转义则记录探测到的编码
            if (1 < pair.length && pair[1].contains("%") && null != (counter = detect(valueBytes, counters))) {
                counter.count++;
            }
        }
        return paramsBytesMap;
    }

    /**
     * 将 URI Encode 的字符串转换为原始的字节数组
     *
     * @param text 包含 uri encode %xx 转义的字符串
     * @return 转义前的字节数组
     */
    private static byte[] decodeURIComponentToBytes(String text) {
        int len = text.length();
        byte[] bytes = new byte[len];
        int offset = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '%':
                    // %xx
                    if (2 < len - i) {
                        int hi = Character.digit(text.charAt(++i), 16);
                        int lo = Character.digit(text.charAt(++i), 16);
                        bytes[offset++] = (byte) (((hi << 4) | lo) & 0xff);
                        break;
                    }
                case '+':
                    bytes[offset++] = 32;           // GBK, UTF-8 英文占1个字节
                    break;
                default:
                    bytes[offset++] = (byte) c;     // GBK, UTF-8 英文占1个字节
            }
        }
        return Arrays.copyOfRange(bytes, 0, offset);
    }

    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /**
     * 解析未转义的参数 (只有 IE 直接在地址栏输入才会不进行转义, 直接提交 GBK 编码).
     *
     * @param params   未转义的字符参数
     * @param counters 字符集统计
     * @return 原始的参数字节
     */
    private static Map<ByteBuffer, List<ByteBuffer>> doParseUnescapeQueryString(final String[] params,
                                                                                final CharsetCounter[] counters) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("parse unescape params: {}", Arrays.toString(params));
        }

        final Map<ByteBuffer, List<ByteBuffer>> paramsBytesMap = new LinkedHashMap<ByteBuffer, List<ByteBuffer>>();
        for (final String param : params) {
            final String[] pair = param.split("=", 2);
            /*-
             * 获取原始字节, 字节 getBytes 会将原始字节转换为 平台默认编码的字节,
             * 因此这里借助每个字符占1个字节的 ISO-8859-1 还原原始字节
             */
            final byte[] nameBytes = pair[0].getBytes(ISO_8859_1);
            final byte[] valueBytes = 1 < pair.length ? pair[1].getBytes(ISO_8859_1) : new byte[0];

            /*-
             * 因为只有 ie 会不编码因此, 获取原始编码后直接 GBK 解码即可
             * 这里为了保险起见, 还是还原后检测下
             */
            final ByteBuffer key = ByteBuffer.wrap(nameBytes);
            List<ByteBuffer> values = paramsBytesMap.get(key);
            if (null == values) {
                paramsBytesMap.put(key, values = new LinkedList<ByteBuffer>());
            }
            values.add(ByteBuffer.wrap(valueBytes));

            CharsetCounter counter;
            if (hasNonAscii(pair[0]) && null != (counter = detect(nameBytes, counters))) {
                counter.count++;
            }
            if (1 < pair.length && hasNonAscii(pair[1]) && null != (counter = detect(valueBytes, counters))) {
                counter.count++;
            }
        }
        return paramsBytesMap;
    }


    private static Map<String, String[]> decodeParamsMap(final Map<ByteBuffer, List<ByteBuffer>> paramsByteMap,
                                                         final Charset charset) {
        final Map<String, String[]> paramsMap = new HashMap<String, String[]>();
        final CharsetDecoder decoder = charset.newDecoder();

        decoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).reset();
        try {
            for (final Map.Entry<ByteBuffer, List<ByteBuffer>> entry : paramsByteMap.entrySet()) {
                final ByteBuffer keyBytes = entry.getKey();
                final List<ByteBuffer> valuesBytes = entry.getValue();

                final String name = decoder.decode(keyBytes).toString();
                final String[] values = new String[valuesBytes.size()];
                for (int j = 0; j < valuesBytes.size(); j++) {
                    values[j] = decoder.decode(valuesBytes.get(j)).toString();
                }
                paramsMap.put(name, values);
            }
        } catch (final CharacterCodingException e) {
            throw new Error(e);
        }
        return paramsMap;
    }

    /**
     * 创建一个字符集对应的统计对象.
     *
     * @param charsets 字符集
     * @return 统计对象
     */
    private static CharsetCounter[] wrap(Charset[] charsets) {
        final CharsetCounter[] counter = new CharsetCounter[charsets.length];
        for (int i = 0; i < counter.length; i++) {
            counter[i] = new CharsetCounter(charsets[i]);
        }
        return counter;
    }

    /**
     * 检测给定字符串的字节数组的字符编码
     * 如果字符编码列表中不存在能正确解析字节数组的字符编码, 将返回 null
     * <p/>
     * 注意: 对于多个字符编码可能相同的字节数组会对应不同的字符, 将按照给定顺序优先匹配
     * eg: GBK 中文 -- Big5 笢; GBK 文 -- Big5 恅 字节是一样的
     * 某个字符在多种编码中占用长度一样时, 该情况比较多
     *
     * @param bytes    字符串的字节数组
     * @param charsets 字符编码列表
     */
    private static CharsetCounter detect(final byte[] bytes, final CharsetCounter[] charsets) {
        for (final CharsetCounter charset : charsets) {
            if (canDecode(bytes, charset.charset)) {
                return charset;
            }
        }
        return null;
    }

    /**
     * 该方法主要利用 字节数组在使用不正确的编码解析后存在字节损失.
     * <p/>
     * (对于不能解析的字节会转换为?等, 再次获取字节数组就会与解析前不同) 的特点进行检测.
     *
     * @param bytes   原始字节
     * @param charset 字符编码
     * @return 是否能正确解析
     */
    private static boolean canDecode(final byte[] bytes, final Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charsets must not be null");
        }

        /**
         * {@link String#getBytes()} 始终是返回这个字符串给定编码的字节数组
         * 如果使用特定字符编码解析字节数组后字符串为乱码, 则 getBytes 获取的是这个乱码的字节数组
         * 也就是说一旦一个字节数组被转换为字符串就无法通过这个字符串再获取原来的字节数组
         */
        final byte[] bytes2 = new String(bytes, charset).getBytes(charset);

        return Arrays.equals(bytes, bytes2);
    }

}
