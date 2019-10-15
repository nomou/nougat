/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.web.util;

import freework.util.Castor;
import freework.io.Path;
import freework.util.StringUtils2;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vacoor
 */
public abstract class WebUtils {
    /**
     * Standard Servlet 2.3+ spec request attributes for include URI and paths.
     * <p>If included via a RequestDispatcher, the current resource will see the
     * originating request. Its own URI and paths are exposed as request attributes.
     */
    public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";
    public static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";
    public static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";
    public static final String INCLUDE_PATH_INFO_ATTRIBUTE = "javax.servlet.include.path_info";
    public static final String INCLUDE_QUERY_STRING_ATTRIBUTE = "javax.servlet.include.query_string";

    /**
     * Standard Servlet 2.4+ spec request attributes for forward URI and paths.
     * <p>If forwarded to via a RequestDispatcher, the current resource will see its
     * own URI and paths. The originating URI and paths are exposed as request attributes.
     */
    public static final String FORWARD_REQUEST_URI_ATTRIBUTE = "javax.servlet.forward.request_uri";
    public static final String FORWARD_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.forward.context_path";
    public static final String FORWARD_SERVLET_PATH_ATTRIBUTE = "javax.servlet.forward.servlet_path";
    public static final String FORWARD_PATH_INFO_ATTRIBUTE = "javax.servlet.forward.path_info";
    public static final String FORWARD_QUERY_STRING_ATTRIBUTE = "javax.servlet.forward.query_string";

    public static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";

    /**
     * Default character encoding to use when <code>request.getCharacterEncoding</code>
     * returns <code>null</code>, according to the Servlet spec.
     *
     * @see ServletRequest#getCharacterEncoding
     */
    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";
    public static final String FIRST_IP_PATTERN_STR = "^((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9]))(?:(?:\\s*,\\s*)?)";
    public static final Pattern FIRST_IP_PATTERN = Pattern.compile(FIRST_IP_PATTERN_STR);

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";

    private static final String[] DEFAULT_PROXY_CLIENT_IP_HEADERS = new String[]{
            X_FORWARDED_FOR, PROXY_CLIENT_IP, WL_PROXY_CLIENT_IP
    };

    public static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";
    private static final String HTTP_MULTIPART_PREFIX = "multipart/";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * 当前请求是否是 异步请求
     *
     * @param request
     * @return
     */
    public static boolean isAsyncRequest(HttpServletRequest request) {
        return ("XMLHttpRequest".equals(request.getHeader("X-Requested-With")) || isTrue(request, "_async"));
    }

    /**
     * 当前请求是否是 Multipart 请求
     */
    public static boolean isMultipartRequest(HttpServletRequest httpRequest) {
        if (!"POST".equals(httpRequest.getMethod())) {
            return false;
        }
        String contentType = httpRequest.getContentType();
        return null != contentType && contentType.toLowerCase().startsWith(HTTP_MULTIPART_PREFIX);
    }

    public static boolean isGzipSupported(HttpServletRequest httpRequest) {
        String requestedEncoding = httpRequest.getHeader(HEADER_ACCEPT_ENCODING);
        return null != requestedEncoding && requestedEncoding.toLowerCase().contains("gzip");
    }

    public static boolean isTrue(ServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        return value != null &&
                (value.equalsIgnoreCase("true") ||
                        value.equalsIgnoreCase("t") ||
                        value.equalsIgnoreCase("1") ||
                        value.equalsIgnoreCase("enabled") ||
                        value.equalsIgnoreCase("y") ||
                        value.equalsIgnoreCase("yes") ||
                        value.equalsIgnoreCase("on"));
    }


    public static String getSessionId(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        return null != session ? session.getId() : null;
    }

    public static HttpServletRequest toHttp(ServletRequest request) {
        HttpServletRequest httpRequest = getNativeRequest(request, HttpServletRequest.class);
        if (null == httpRequest) {
            throw new IllegalStateException("ServletRequest [" + request + "] is not a HttpServletRequest instance");
        }
        return httpRequest;
    }

    public static HttpServletResponse toHttp(ServletResponse response) {
        HttpServletResponse httpResponse = getNativeResponse(response, HttpServletResponse.class);
        if (null == httpResponse) {
            throw new IllegalStateException("ServletResponse [" + response + "] is not a HttpServletResponse instance");
        }
        return httpResponse;
    }

    public static <T extends ServletRequest> T getNativeRequest(ServletRequest servletRequest, Class<T> requiredType) {
        if (null != requiredType) {
            if (requiredType.isInstance(servletRequest)) {
                return requiredType.cast(servletRequest);
            }
            if (servletRequest instanceof ServletRequestWrapper) {
                return getNativeRequest(((ServletRequestWrapper) servletRequest).getRequest(), requiredType);
            }
        }
        return null;
    }

    public static <T extends ServletResponse> T getNativeResponse(ServletResponse servletResponse, Class<T> requiredType) {
        if (null != requiredType) {
            if (requiredType.isInstance(servletResponse)) {
                return requiredType.cast(servletResponse);
            }
            if (servletResponse instanceof ServletResponseWrapper) {
                return getNativeResponse(((ServletResponseWrapper) servletResponse).getResponse(), requiredType);
            }
        }
        return null;
    }

    public static void clearRequestErrorAttributes(ServletRequest request) {
        request.removeAttribute("javax.servlet.error.status_code");
        request.removeAttribute("javax.servlet.error.exception_type");
        request.removeAttribute("javax.servlet.error.message");
        request.removeAttribute("javax.servlet.error.exception");
        request.removeAttribute("javax.servlet.error.request_uri");
        request.removeAttribute("javax.servlet.error.servlet_name");
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String[]> getUnprefixedParameters(ServletRequest request, String prefix) {
        prefix = null != prefix ? prefix : "";

        Map<String, String[]> params = new TreeMap<String, String[]>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (null != paramNames && paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if ("".equals(prefix) || paramName.startsWith(prefix)) {
                String unprefixed = paramName.substring(prefix.length());
                String[] values = request.getParameterValues(paramName);
                params.put(unprefixed, values);
            }
        }
        return params;
    }

    public static String urlAppend(String url, String text) {
        if (!StringUtils2.hasText(url)) {
            return url;
        }
        return url + (-1 == url.indexOf('?') ? '?' : '&') + text;
    }

    /**
     * Return the request URI for the given request, detecting an forward and include request
     * URL if called within a RequestDispatcher forward or include.
     * <p>As the value returned by <code>request.getRequestURI()</code> is <i>not</i>
     * decoded by the servlet container, this method will decode it.
     * <p>The URI that the web container resolves <i>should</i> be correct, but some
     * containers like JBoss/Jetty incorrectly include ";" strings like ";jsessionid"
     * in the URI. This method cuts off such incorrect appendices.
     *
     * @param request current HTTP request
     * @return the request URI
     */
    public static String getRequestUri(HttpServletRequest request) {
        String uri = (String) request.getAttribute(FORWARD_REQUEST_URI_ATTRIBUTE);
        if (uri == null) {
            uri = (String) request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE);
        }
        if (uri == null) {
            uri = request.getRequestURI();
        }
        return normalize(decodeAndCleanUriString(request, uri));
    }

    /**
     * Retrieves the current request servlet path.
     * Deals with differences between servlet specs (2.2 vs 2.3+)
     *
     * @param request the request
     * @return the servlet path
     */
    public static String getServletPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String requestUri = request.getRequestURI();

        // Detecting other characters that the servlet container cut off (like anything after ';')
        if (requestUri != null && servletPath != null && !requestUri.endsWith(servletPath)) {
            int pos = requestUri.indexOf(servletPath);
            if (pos > -1) {
                servletPath = requestUri.substring(requestUri.indexOf(servletPath));
            }
        }

        if (null != servletPath && !"".equals(servletPath)) {
            return servletPath;
        }

        int startIndex = "".equals(request.getContextPath()) ? 0 : request.getContextPath().length();
        int endIndex = request.getPathInfo() == null ? requestUri.length() : requestUri.lastIndexOf(request.getPathInfo());

        if (startIndex > endIndex) { // this should not happen
            endIndex = startIndex;
        }

        return requestUri.substring(startIndex, endIndex);
    }

    /**
     * Return the context path for the given request, detecting an include request
     * URL if called within a RequestDispatcher include.
     * <p>As the value returned by <code>request.getContextPath()</code> is <i>not</i>
     * decoded by the servlet container, this method will decode it.
     *
     * @param request current HTTP request
     * @return the context path
     */
    public static String getContextPath(HttpServletRequest request) {
        String contextPath = (String) request.getAttribute(INCLUDE_CONTEXT_PATH_ATTRIBUTE);
        if (null == contextPath) {
            contextPath = request.getContextPath();
        }
        if ("/".equals(contextPath)) {
            // Invalid case, but happens for includes on Jetty: silently adapt it.
            contextPath = "";
        }
        return decodeRequestString(request, contextPath);
    }

    /**
     * Return the path within the web application for the given request.
     * Detects include request URL if called within a RequestDispatcher include.
     * <p/>
     * For example, for a request to URL
     * <p/>
     * <code>http://www.somehost.com/myapp/my/url.jsp</code>,
     * <p/>
     * for an application deployed to <code>/mayapp</code> (the application's context path), this method would return
     * <p/>
     * <code>/my/url.jsp</code>.
     *
     * @param request current HTTP request
     * @return the path within the web application
     */
    public static String getPathWithinApplication(HttpServletRequest request) {
        String contextPath = getContextPath(request);
        String requestUri = getRequestUri(request);
        if (StringUtils2.startsWith(requestUri, contextPath, true)) {
            // Normal case: URI contains context path.
            String path = requestUri.substring(contextPath.length());
            return (StringUtils2.hasText(path) ? path : "/");
        } else {
            // Special case: rather unusual.
            return requestUri;
        }
    }

    /**
     * 获取当前应用的 URL
     * <p/>
     * For example: http://www.some.host.com/myapp
     *
     * @param request current HTTP request
     * @return the path for web application
     */
    public static String getApplicationUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();

        return scheme + "://" + host + (port == 80 ? "" : ":" + port) + (null != contextPath && contextPath.length() > 0 ? contextPath : "");
    }

    /**
     * @param request               current HTTP request
     * @param pathWithinApplication the path within the web application
     * @return the path without the web application
     */
    public static String getUrlWithoutApplication(HttpServletRequest request, String pathWithinApplication) {
        String applicationUrl = getApplicationUrl(request);
        if (null == pathWithinApplication) {
            return applicationUrl;
        }
        if (pathWithinApplication.startsWith("/")) {
            pathWithinApplication = pathWithinApplication.substring(1);
        }
        if (pathWithinApplication.length() > 0) {
            applicationUrl += applicationUrl.endsWith("/") ? pathWithinApplication : ("/" + pathWithinApplication);
        }
        return applicationUrl;
    }

    public static File getTempDir(ServletContext context) {
        File tmpDir = (File) context.getAttribute(TEMP_DIR_CONTEXT_ATTRIBUTE);
        tmpDir = null != tmpDir ? tmpDir : new File(System.getProperty(TEMP_DIR_SYSTEM_PROPERTY));
        return tmpDir.exists() ? tmpDir : null;
    }

    public static String getRealPath(ServletContext servletContext, String path) {
        String rootPath = servletContext.getRealPath("/");
        return new File(rootPath, path).getAbsolutePath();
    }

    public static String getRequiredRealPath(ServletContext servletContext, String path) throws FileNotFoundException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        String realPath = servletContext.getRealPath(path);
        if (null == realPath) {
            throw new FileNotFoundException("ServletContext resource [" + path + "] cannot be resolved to absolute file path - " + "web application archive not expanded?");
        } else {
            return realPath;
        }
    }

    public static String normalize(String path) {
        return Path.normalize(path);
    }

    /**
     * Decode the supplied URI string and strips any extraneous portion after a ';'.
     *
     * @param request the incoming HttpServletRequest
     * @param uri     the application's URI string
     * @return the supplied URI string stripped of any extraneous portion after a ';'.
     */
    private static String decodeAndCleanUriString(HttpServletRequest request, String uri) {
        uri = decodeRequestString(request, uri);
        int semicolonIndex = uri.indexOf(';');
        return (semicolonIndex != -1 ? uri.substring(0, semicolonIndex) : uri);
    }

    /**
     * Decode the given source string with a URLDecoder. The encoding will be taken
     * from the request, falling back to the default "ISO-8859-1".
     * <p>The default implementation uses <code>URLDecoder.decode(input, enc)</code>.
     *
     * @param request current HTTP request
     * @param source  the String to decode
     * @return the decoded String
     * @see #DEFAULT_CHARACTER_ENCODING
     * @see ServletRequest#getCharacterEncoding
     * @see URLDecoder#decode(String, String)
     * @see URLDecoder#decode(String)
     */
    @SuppressWarnings({"deprecation"})
    public static String decodeRequestString(HttpServletRequest request, String source) {
        String enc = determineEncoding(request);
        try {
            return URLDecoder.decode(source, enc);
        } catch (UnsupportedEncodingException ex) {
            /*
            if (log.isWarnEnabled()) {
                log.warn("Could not decode request string [" + source + "] with encoding '" + enc +
                        "': falling back to platform default encoding; exception message: " + ex.getMessage());
            }
            */
            return URLDecoder.decode(source);
        }
    }

    /**
     * Determine the encoding for the given request.
     * Can be overridden in subclasses.
     * <p>The default implementation checks the request's
     * {@link ServletRequest#getCharacterEncoding() character encoding}, and if that
     * <code>null</code>, falls back to the {@link #DEFAULT_CHARACTER_ENCODING}.
     *
     * @param request current HTTP request
     * @return the encoding for the request (never <code>null</code>)
     * @see ServletRequest#getCharacterEncoding()
     */
    protected static String determineEncoding(HttpServletRequest request) {
        String enc = request.getCharacterEncoding();
        if (enc == null) {
            enc = DEFAULT_CHARACTER_ENCODING;
        }
        return enc;
    }


    public static void expired(HttpServletResponse resp) {
        // Set standard HTTP/1.1 no-cache headers.
        resp.setHeader("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        resp.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0L);
    }

    public static boolean isIncludeRequest(ServletRequest request) {
        return null != request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE);
    }

    public static boolean isForwardRequest(ServletRequest request) {
        return null != request.getAttribute(FORWARD_REQUEST_URI_ATTRIBUTE);
    }

    /**
     * 判断请求的 servlet 是否为前缀映射
     * <p/>
     * servlet mapping 有两种匹配方式: 前缀匹配和后缀匹配
     * <ul>
     * <li>前缀匹配(eg: /prefix/*) : requestURI=/prefix/decode/b, servlet path: /prefix, path info: /decode/b</li>
     * <li>后缀匹配(eg: *.action) : requestURI=/prefix/decode/b.action, servlet path: /prefix/decode/b.action, path info: null</li>
     * </ul>
     *
     * @param request
     * @return
     */
    public static boolean isPrefixServletMapping(HttpServletRequest request) {
        String pathInfo = StringUtils2.trimToNull(request.getPathInfo());
        if (pathInfo != null) {
            return true;
        } else {
            /**
             * 对于前缀匹配 /prefix, 当 requestURI = /prefix 时, pathInfo 也为null
             * 此时通过 servletPath 是否是后缀匹配判断
             */
            String servletPath = StringUtils2.trimToEmpty(request.getServletPath());
            int index = servletPath.lastIndexOf("/");

            return servletPath.indexOf(".", index + 1) < 0;
        }
    }

    /**
     * 使用给定的 ServletConfig 来配置 Servlet 实例.
     * <p/>
     * 该方法将 config 中参数调用对应的 servlet setter 进行注入.
     *
     * @param servlet Servlet 实例
     * @param config  ServletConfig
     */
    public static void configure(final Servlet servlet, final ServletConfig config) {
        configure(servlet, config, (char) 0);
    }

    public static void configure(final Filter filter, final FilterConfig config) {
        configure(filter, config, (char) 0);
    }

    @SuppressWarnings("unchecked")
    public static void configure(final Servlet servlet, final ServletConfig config, final char delimiter) {
        final Class<? extends Servlet> type = servlet.getClass();
        final Enumeration<String> names = config.getInitParameterNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final String value = config.getInitParameter(name);
            final String prop = 0 < delimiter
                    ? StringUtils2.delimitedToCamelCase(name, delimiter, true)
                    : StringUtils2.capitalize(name);

            final Method[] methods = type.getMethods();
            for (final Method method : methods) {
                if (method.getName().equals("set" + prop)) {
                    final Class<?>[] parameterTypes = method.getParameterTypes();
                    final Object arg = Castor.cast(value, parameterTypes[0]);
                    if (1 == parameterTypes.length && null != arg) {
                        try {
                            method.invoke(servlet, arg);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        } catch (InvocationTargetException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void configure(final Filter filter, final FilterConfig config, final char delimiter) {
        final Class<? extends Filter> type = filter.getClass();
        final Enumeration<String> names = config.getInitParameterNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final String value = config.getInitParameter(name);
            final String prop = 0 < delimiter ? StringUtils2.delimitedToCamelCase(name, delimiter, true) : name;

            final Method[] methods = type.getMethods();
            for (final Method method : methods) {
                if (method.getName().equals("set" + prop)) {
                    final Class<?>[] parameterTypes = method.getParameterTypes();
                    final Object arg = Castor.cast(value, parameterTypes[0]);
                    if (1 == parameterTypes.length && null != arg) {
                        try {
                            method.invoke(filter, arg);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        } catch (InvocationTargetException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }
        }
    }

    private static final String LOOPBACK_IPV4 = "127.0.0.1";
    private static final String LOOPBACK_IPV6 = "0:0:0:0:0:0:0:1";

    public static String getRemoteHost(final ServletRequest request) {
        return getRemoteHost(request, false);
    }

    public static String getRemoteAddr(final ServletRequest request) {
        return getRemoteAddr(request, false);
    }

    public static String getRemoteHost(final ServletRequest request, final boolean skipReverseProxies) {
        String host = null;
        if (!skipReverseProxies && request instanceof HttpServletRequest) {
            host = getRemoteAddrFromProxies((HttpServletRequest) request);
        }
        host = null != host ? host : request.getRemoteHost();
        return (LOOPBACK_IPV6.equals(host)) ? LOOPBACK_IPV4 : host;
    }

    /**
     * 获取请求客户端请求的 IP
     * <p/>
     * 注意: 当 skipReverseProxies = true, 返回的 IP 是可信的.
     *
     * @param request            HttpServletRequest
     * @param skipReverseProxies 是否跳过非高匿代理服务器 IP (优先通过 XFF 等请求头获取)
     * @return 客户端请求 IP
     */
    public static String getRemoteAddr(final ServletRequest request, final boolean skipReverseProxies) {
        String addr = null;
        if (!skipReverseProxies && request instanceof HttpServletRequest) {
            addr = getRemoteAddrFromProxies((HttpServletRequest) request);
        }
        addr = null != addr ? addr : request.getRemoteAddr();
        return (LOOPBACK_IPV6.equals(addr)) ? LOOPBACK_IPV4 : addr;
    }

    public static String getRemoteAddrFromProxies(final HttpServletRequest httpRequest) {
        return getRemoteAddrFromProxies(httpRequest, DEFAULT_PROXY_CLIENT_IP_HEADERS);
    }

    /**
     * 从请求头中获取远程客户端地址.
     * <p/>
     * 当指定了 proxiedHeaders 时, 该方法会优先从 HTTP 请求头中解析 IP 信息, 返回的结果是不可信的(HTTP请求头可能伪造),
     * 因此请谨慎用于使用 ip 限制的功能, eg: 投票系统使用该 Header 来限制用户投票, 伪造后限制就失效
     *
     * @param httpRequest    HttpServletRequest
     * @param proxiedHeaders 用来提供真实IP的请求头, eg: X-Forwarded-For
     * @return 客户端 IP 地址
     */
    public static String getRemoteAddrFromProxies(final HttpServletRequest httpRequest, String... proxiedHeaders) {
        String addr = null;
        for (final String name : proxiedHeaders) {
            final String header = httpRequest.getHeader(name);
            if (null == header || 0 == header.length() || "unknown".equalsIgnoreCase(header)) {
                continue;
            }
            /*-
             * 如果经过了多级反向代理, X-Forwarded-For 会有多个值, 用户 IP 为第一个值(第一个非高匿代理服务器IP)
             * XFF 等 HTTP 头 可伪造, 进行过滤, 防止SQL注入( X-Forwarded-For: 192.168.a.b' or 1=)
             */
            final Matcher matcher = FIRST_IP_PATTERN.matcher(header);
            if (matcher.find()) {
                addr = matcher.group(1);
                break;
            }
        }
        // addr = ("0:0:0:0:0:0:0:1".equals(addr)) ? "127.0.0.1" : addr;
        return addr;
    }

    private WebUtils() {
    }
}
