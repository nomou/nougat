package freework.web.servlet;

import freework.reflect.Parameter;
import freework.web.servlet.annotation.After;
import freework.web.servlet.annotation.Before;
import freework.web.servlet.annotation.CookieParam;
import freework.web.servlet.annotation.Delete;
import freework.web.servlet.annotation.Get;
import freework.web.servlet.annotation.Head;
import freework.web.servlet.annotation.HeaderParam;
import freework.web.servlet.annotation.Options;
import freework.web.servlet.annotation.PathParam;
import freework.web.servlet.annotation.Post;
import freework.web.servlet.annotation.Put;
import freework.web.servlet.annotation.RequestParam;
import freework.web.servlet.annotation.RequestPart;
import freework.web.util.UriTemplateParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Servlet路由.
 *
 * @author vacoor
 * @since 1.0
 */
@Slf4j
public class HttpServletRouter {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String HEAD = "HEAD";
    private static final String OPTIONS = "OPTIONS";

    /**
     * 默认处理的Url.
     */
    private static final String DEFAULT_HANDLE_URL = "";

    /**
     * 默认Url-Pattern.
     */
    private static final String[] DEFAULT_URL_PATTERNS = {DEFAULT_HANDLE_URL};

    /**
     * Uri模板匹配度比较器.
     */
    private static final Comparator<UriTemplateParser> URI_TEMPLATE_COMPARATOR = new Comparator<UriTemplateParser>() {
        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(final UriTemplateParser o1, final UriTemplateParser o2) {
            final int i = o1.getNumberOfRegexGroups() - o2.getNumberOfRegexGroups();
            return 0 != i ? i : o1.getNumberOfExplicitRegexes() - o2.getNumberOfExplicitRegexes();
        }
    };

    /**
     * 前置处理器优先级比较器.
     */
    private static final Comparator<Method> PRE_INTERCEPTOR_COMPARATOR = new Comparator<Method>() {
        @Override
        public int compare(final Method m1, final Method m2) {
            final Before b1 = m1.getAnnotation(Before.class);
            final Before b2 = m2.getAnnotation(Before.class);
            return b1.priority() - b2.priority();
        }
    };

    /**
     * 后置处理器优先级比较器.
     */
    private static final Comparator<Method> POST_INTERCEPTOR_COMPARATOR = new Comparator<Method>() {
        @Override
        public int compare(final Method m1, final Method m2) {
            final After b1 = m1.getAnnotation(After.class);
            final After b2 = m2.getAnnotation(After.class);
            return b1.priority() - b2.priority();
        }
    };

    /**
     * 处理结果.
     */
    public class Returning {
        /**
         * 处理方法.
         */
        final Method target;

        /**
         * 处理返回值.
         */
        final Object value;

        /**
         * 处理异常.
         */
        final Throwable throwing;

        private Returning(final Method target, final Object value, final Throwable throwing) {
            this.target = target;
            this.value = value;
            this.throwing = throwing;
        }

        public Method getTarget() {
            return target;
        }

        public Object getReturn() {
            return value;
        }

        public Throwable getThrowing() {
            return throwing;
        }
    }

    /**
     * 处理器映射KEY.
     */
    private static class MappingKey {
        /**
         * Http请求方法.
         */
        private final String method;

        /**
         * Url模式.
         */
        private final String urlPattern;

        /**
         * Private constructor.
         *
         * @param method     Http请求方法
         * @param urlPattern 映射Url模式
         */
        private MappingKey(final String method, final String urlPattern) {
            this.method = method;
            this.urlPattern = urlPattern;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object target) {
            if (this == target) {
                return true;
            }
            if (target == null || getClass() != target.getClass()) {
                return false;
            }

            final MappingKey that = (MappingKey) target;
            if (method != null ? !method.equals(that.method) : that.method != null) {
                return false;
            }
            return urlPattern != null ? urlPattern.equals(that.urlPattern) : that.urlPattern == null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int result = method != null ? method.hashCode() : 0;
            result = 31 * result + (urlPattern != null ? urlPattern.hashCode() : 0);
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return method + " " + urlPattern;
        }
    }

    /**
     * 路由的Servlet.
     */
    private final HttpServlet target;

    /**
     * 映射的处理方法.
     */
    private final Map<MappingKey, Method> mappedHandlers;

    /**
     * 前置处理器.
     */
    private final List<Method> preInterceptors;

    /**
     * 后置处理器.
     */
    private final List<Method> postInterceptors;

    /**
     * 调用处理信息.
     */
    private final ThreadLocal<Returning> invoked = new ThreadLocal<>();

    /**
     * 根据给定信息创建一个ServletRouter.
     *
     * @param target           需要路由的Servlet
     * @param mappedHandlers   映射的处理方法
     * @param preInterceptors  前置处理方法
     * @param postInterceptors 后置处理方法
     */
    private HttpServletRouter(final HttpServlet target,
                              final Map<MappingKey, Method> mappedHandlers,
                              final List<Method> preInterceptors,
                              final List<Method> postInterceptors) {
        this.target = target;
        this.mappedHandlers = mappedHandlers;
        this.preInterceptors = preInterceptors;
        this.postInterceptors = postInterceptors;
    }

    /**
     * 执行内部路由.
     *
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @throws ServletException 如果http请求不能处理
     * @throws IOException      如果输入或输出发生错误
     */
    public void route(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        final String method = httpRequest.getMethod();
        final String pathInfo = httpRequest.getPathInfo();
        final String urlPattern = matches(httpRequest.getMethod(), httpRequest.getPathInfo(), mappedHandlers.keySet());
        final Method handler = mappedHandlers.get(new MappingKey(method.toUpperCase(), urlPattern));
        if (null != handler) {
            if (this.doIntercept(Before.class, preInterceptors, pathInfo, urlPattern, httpRequest, httpResponse)) {
                if (!this.hasInterceptors(After.class, httpRequest.getMethod(), postInterceptors)) {
                    // 不存在后置处理器, 直接执行.
                    this.doHandle(httpRequest, httpResponse, pathInfo, urlPattern, handler);
                } else {
                    // 存在后置处理器, 则异常交由后置处理器处理.
                    Object ret = null;
                    Throwable throwing = null;
                    try {
                        ret = this.doHandle(httpRequest, httpResponse, pathInfo, urlPattern, handler);
                    } catch (final Throwable ex) {
                        if (log.isDebugEnabled()) {
                            log.debug("route handler happen error", ex);
                        }
                        throwing = ex;
                    }

                    try {
                        invoked.set(new Returning(handler, ret, throwing));
                        this.doIntercept(After.class, postInterceptors, pathInfo, urlPattern, httpRequest, httpResponse);
                    } finally {
                        invoked.remove();
                    }
                }
            }
        } else {
            // 404
            httpResponse.sendError(404);
        }
    }

    /**
     * 获取当前处理结果.
     *
     * <p>此方法只有在{@link After}标注的方法中可用</p>
     *
     * @return 处理结果
     */
    public Returning getReturning() {
        return invoked.get();
    }

    /**
     * 获取最佳匹配的Url-Pattern.
     *
     * @param httpMethod Http请求方法
     * @param pathInfo   Http请求的Servlet pathInfo
     * @param candidates 需要匹配的Url-Pattern信息
     * @return 如果存在可以匹配http请求方法和pathInfo的urlPattern则返回, 否则返回null
     */
    private String matches(final String httpMethod, final String pathInfo, final Set<MappingKey> candidates) {
        final String finalPath = null != pathInfo ? pathInfo : DEFAULT_HANDLE_URL;
        final List<UriTemplateParser> ret = new ArrayList<>(candidates.size());
        for (final MappingKey candidate : candidates) {
            if (!candidate.method.equalsIgnoreCase(httpMethod)) {
                continue;
            }

            final UriTemplateParser parser = new UriTemplateParser(candidate.urlPattern);
            if (parser.getPattern().matcher(finalPath).matches()) {
                ret.add(parser);
            }
        }
        Collections.sort(ret, URI_TEMPLATE_COMPARATOR);
        return !ret.isEmpty() ? ret.iterator().next().getTemplate() : null;
    }

    /**
     * 执行拦截逻辑.
     *
     * @param interceptType 拦截类型
     * @param candidates    所有拦截器
     * @param pathInfo      请求的路径
     * @param urlPattern    匹配的Url-Pattern
     * @param httpRequest   the http request
     * @param httpResponse  the http response
     * @return 是否需要继续执行下一个拦截器或处理器(如果true则继续执行, 否则立即终止)
     * @throws ServletException 如果http请求不能处理
     * @throws IOException      如果输入或输出发生错误
     */
    private boolean doIntercept(final Class<? extends Annotation> interceptType, final List<Method> candidates,
                                final String pathInfo, final String urlPattern, final HttpServletRequest httpRequest,
                                final HttpServletResponse httpResponse) throws ServletException, IOException {
        for (final Method interceptor : candidates) {
            final String[] interceptMethods = getInterceptMethods(interceptor, interceptType);
            final List<String> upperInterceptMethods = new ArrayList<>(interceptMethods.length);
            for (final String method : interceptMethods) {
                upperInterceptMethods.add(method.toUpperCase());
            }

            if (!upperInterceptMethods.isEmpty() && !upperInterceptMethods.contains(httpRequest.getMethod().toUpperCase())) {
                continue;
            }

            final Parameter[] params = Parameter.lookup(interceptor);
            final Object[] args = resolveArguments(params, pathInfo, urlPattern, httpRequest, httpResponse);

            final Object ret = doInvoke(interceptor, this.target, args);
            if (boolean.class.isAssignableFrom(interceptor.getReturnType())) {
                // 当前拦截器返回值是false时表示立即终止执行.
                if (!Boolean.class.cast(ret)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 是否存在拦截给定http方法的给定类型的拦截器.
     *
     * @param interceptType 拦截类型
     * @param httpMethod    Http请求方法
     * @param candidates    候选拦截器
     * @return 是否存在符合条件的拦截器
     */
    private boolean hasInterceptors(final Class<? extends Annotation> interceptType, final String httpMethod, final List<Method> candidates) {
        for (final Method interceptor : candidates) {
            final String[] interceptMethods = getInterceptMethods(interceptor, interceptType);
            final List<String> upperInterceptMethods = new ArrayList<>(interceptMethods.length);
            for (final String method : interceptMethods) {
                upperInterceptMethods.add(method.toUpperCase());
            }

            if (!upperInterceptMethods.isEmpty() && !upperInterceptMethods.contains(httpMethod.toUpperCase())) {
                continue;
            }
            return true;
        }
        return false;
    }

    private String[] getInterceptMethods(final Method interceptor, final Class<? extends Annotation> interceptType) {
        final Annotation ann = interceptor.getAnnotation(interceptType);

        String[] interceptMethods;
        if (Before.class.equals(interceptType)) {
            interceptMethods = Before.class.cast(ann).methods();
        } else if (After.class.equals(interceptType)) {
            interceptMethods = After.class.cast(ann).methods();
        } else {
            throw new IllegalArgumentException(String.format("illegal intercept type: %s", interceptType));
        }
        return interceptMethods;
    }

    /**
     * 执行处理逻辑.
     *
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @param pathInfo     请求的路径
     * @param urlPattern   匹配的Url-Pattern
     * @param handler      映射的处理方法
     * @throws ServletException 如果http请求不能处理
     * @throws IOException      如果输入或输出发生错误
     */
    private Object doHandle(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
                            final String pathInfo, final String urlPattern, final Method handler) throws ServletException, IOException {
        final Parameter[] params = Parameter.lookup(handler);
        final Object[] args = resolveArguments(params, pathInfo, urlPattern, httpRequest, httpResponse);
        return doInvoke(handler, this.target, args);
    }

    /**
     * 解析所有方法参数.
     *
     * @param parameters   方法参数
     * @param pathInfo     请求的 servlet path info
     * @param urlPattern   匹配的Url-Pattern
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return 所有解析的参数
     * @throws ServletException 如果http请求不能处理
     * @throws IOException      如果输入或输出发生错误
     */
    private Object[] resolveArguments(final Parameter[] parameters, final String pathInfo, final String urlPattern,
                                      final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws IOException, ServletException {
        final Object[] resolved = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            resolved[i] = resolveArgument(parameters[i], pathInfo, urlPattern, httpRequest, httpResponse);
        }
        return resolved;
    }

    /**
     * 解析单个方法参数.
     *
     * @param parameter    方法参数
     * @param pathInfo     请求的 servlet path info
     * @param urlPattern   匹配的Url-Pattern
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return 所有解析的参数
     * @throws ServletException 如果http请求不能处理
     * @throws IOException      如果输入或输出发生错误
     */
    private Object resolveArgument(final Parameter parameter, final String pathInfo, final String urlPattern,
                                   final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws IOException, ServletException {
        final Class<?> paramType = parameter.getType();
        if (ServletRequest.class.isAssignableFrom(paramType)) {
            return httpRequest;
        }
        if (ServletResponse.class.isAssignableFrom(paramType)) {
            return httpResponse;
        }
        if (HttpSession.class.isAssignableFrom(paramType)) {
            return httpRequest.getSession();
        }

        final RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (null != requestParam) {
            return resolveRequestParam(requestParam, paramType, httpRequest);
        }

        final RequestPart requestPart = parameter.getAnnotation(RequestPart.class);
        if (null != requestPart) {
            return resolveRequestPart(requestPart, paramType, httpRequest);
        }

        final PathParam pathParam = parameter.getAnnotation(PathParam.class);
        if (null != pathParam) {
            return resolvePathParam(pathParam, paramType, pathInfo, urlPattern);
        }

        final HeaderParam headerParam = parameter.getAnnotation(HeaderParam.class);
        if (null != headerParam) {
            return resolveHeaderParam(headerParam, paramType, httpRequest);
        }

        final CookieParam cookieParam = parameter.getAnnotation(CookieParam.class);
        if (null != cookieParam) {
            return resolveCookieParam(cookieParam, paramType, httpRequest);
        }

        return null;
    }

    /**
     * 解析{@link RequestParam} 参数.
     *
     * @param param       参数标注的{@link RequestParam}
     * @param type        参数类型
     * @param httpRequest the http request
     * @return 解析后的参数值, 如果不存在返回null
     */
    private Object resolveRequestParam(final RequestParam param, final Class<?> type, final HttpServletRequest httpRequest) {
        final String name = param.value();
        if (String.class.isAssignableFrom(type)) {
            return httpRequest.getParameter(name);
        }
        if (String[].class.isAssignableFrom(type)) {
            return httpRequest.getParameterValues(name);
        }
        throw new IllegalStateException(String.format("the parameter '%s' annotated by %s must be a String or String[] type", name, RequestParam.class.getName()));
    }


    /**
     * 解析{@link RequestPart} 参数.
     *
     * @param param       参数标注的{@link RequestPart}
     * @param type        参数类型
     * @param httpRequest the http request
     * @return 解析后的参数值, 如果不存在返回null
     */
    private Object resolveRequestPart(final RequestPart param, final Class<?> type, final HttpServletRequest httpRequest) throws ServletException, IOException {
        final String name = param.value();
        if (isMultipart(httpRequest)) {
            if (Part.class.isAssignableFrom(type)) {
                return httpRequest.getPart(name);
            }
            throw new IllegalStateException(String.format("the parameter '%s' annotated by %s must be a javax.servlet.http.Part type", name, RequestPart.class.getName()));
        }
        throw new IllegalStateException("the request must be a multipart request");
    }

    /**
     * 解析{@link PathParam} 参数.
     *
     * @param param      参数标注的{@link PathParam}
     * @param type       参数类型
     * @param pathInfo   servlet path info
     * @param urlPattern 匹配的Url-Pattern
     * @return 解析后的参数值, 如果不存在返回null
     */
    private Object resolvePathParam(final PathParam param, final Class<?> type, final String pathInfo, final String urlPattern) {
        final String name = param.value();
        if (String.class.isAssignableFrom(type)) {
            final UriTemplateParser parser = new UriTemplateParser(urlPattern);
            final Matcher matcher = parser.getPattern().matcher(pathInfo);
            if (matcher.matches()) {
                final List<String> names = parser.getNames();
                for (int i = 0; i < names.size(); i++) {
                    if (names.get(i).equals(name)) {
                        return matcher.group(i + 1);
                    }
                }
            }
            return null;
        }
        throw new IllegalStateException(String.format("the parameter '%s' annotated by %s must be a String type", name, PathParam.class.getName()));
    }

    /**
     * 解析{@link HeaderParam} 参数.
     *
     * @param param       参数标注的{@link HeaderParam}
     * @param type        参数类型
     * @param httpRequest the http request
     * @return 解析后的参数值, 如果不存在返回null
     */
    private Object resolveHeaderParam(final HeaderParam param, final Class<?> type, final HttpServletRequest httpRequest) {
        final String name = param.value();
        if (String.class.isAssignableFrom(type) || String[].class.isAssignableFrom(type)) {
            final Enumeration<String> headers = httpRequest.getHeaders(name);
            final List<String> values = new ArrayList<>();
            while (headers.hasMoreElements()) {
                values.add(headers.nextElement());
            }
            if (values.isEmpty()) {
                return null;
            }
            return String.class.isAssignableFrom(type) ? values.iterator().next() : values;
        }
        throw new IllegalStateException(String.format("the parameter '%s' annotated by %s must be a String or String[] type", name, HeaderParam.class.getName()));
    }

    /**
     * 解析{@link CookieParam} 参数.
     *
     * @param param       参数标注的{@link CookieParam}
     * @param type        参数类型
     * @param httpRequest the http request
     * @return 解析后的参数值, 如果不存在返回null
     */
    private Object resolveCookieParam(final CookieParam param, final Class<?> type, final HttpServletRequest httpRequest) {
        final String name = param.value();
        if (String.class.isAssignableFrom(type) || String[].class.isAssignableFrom(type)) {
            final javax.servlet.http.Cookie[] cookies = httpRequest.getCookies();
            final List<String> values = new ArrayList<>();
            for (final javax.servlet.http.Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    values.add(cookie.getValue());
                }
            }

            if (values.isEmpty()) {
                return null;
            }
            return String.class.isAssignableFrom(type) ? values.iterator().next() : values;
        }
        throw new IllegalStateException(String.format("the parameter '%s' annotated by %s must be a String or String[] type", name, CookieParam.class.getName()));
    }

    /**
     * 执行给定的方法.
     *
     * @param invoker   待执行方法
     * @param target    方法this对象
     * @param arguments 方法参数
     * @return 执行返回
     */
    private Object doInvoke(final Method invoker, final Object target, final Object... arguments) {
        try {
            if (!invoker.isAccessible()) {
                invoker.setAccessible(true);
            }
            return invoker.invoke(target, arguments);
        } catch (final IllegalAccessException ex) {
            throw new IllegalStateException(String.format("Could not access constructor/field/method: %s", ex.getMessage()), ex);
        } catch (final InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private boolean isMultipart(HttpServletRequest request) {
        // Same check as in Commons FileUpload...
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String contentType = request.getContentType();
        return StringUtils.startsWithIgnoreCase(contentType, "multipart/");
    }

    /**
     * 为给定的HttpServlet创建一个路由对象.
     *
     * @param target 使用路由注解标注的Servlet
     * @return 路由对象
     */
    public static HttpServletRouter create(final HttpServlet target) {
        final Class<? extends HttpServlet> type = target.getClass();
        final Method[] methods = type.getDeclaredMethods();

        final Map<MappingKey, Method> mapping = new HashMap<>();
        final List<Method> preInterceptors = new ArrayList<>();
        final List<Method> postInterceptors = new ArrayList<>();

        for (final Method method : methods) {
            if (method.isAnnotationPresent(Get.class)) {
                final String[] urlPatterns = method.getAnnotation(Get.class).value();
                addMapping(method, GET, urlPatterns, mapping);
            }
            if (method.isAnnotationPresent(Post.class)) {
                final String[] urlPatterns = method.getAnnotation(Post.class).value();
                addMapping(method, POST, urlPatterns, mapping);
            }
            if (method.isAnnotationPresent(Put.class)) {
                final String[] urlPatterns = method.getAnnotation(Put.class).value();
                addMapping(method, PUT, urlPatterns, mapping);
            }
            if (method.isAnnotationPresent(Delete.class)) {
                final String[] urlPatterns = method.getAnnotation(Delete.class).value();
                addMapping(method, DELETE, urlPatterns, mapping);
            }
            if (method.isAnnotationPresent(Head.class)) {
                final String[] urlPatterns = method.getAnnotation(Head.class).value();
                addMapping(method, HEAD, urlPatterns, mapping);
            }
            if (method.isAnnotationPresent(Options.class)) {
                final String[] urlPatterns = method.getAnnotation(Options.class).value();
                addMapping(method, OPTIONS, urlPatterns, mapping);
            }
            if (method.isAnnotationPresent(Before.class)) {
                preInterceptors.add(method);
            }

            if (method.isAnnotationPresent(After.class)) {
                postInterceptors.add(method);
            }
        }

        Collections.sort(preInterceptors, PRE_INTERCEPTOR_COMPARATOR);
        Collections.sort(postInterceptors, POST_INTERCEPTOR_COMPARATOR);

        return new HttpServletRouter(target, mapping, preInterceptors, postInterceptors);
    }


    private static void addMapping(final Method method, final String httpMethod, final String[] urlPatterns, final Map<MappingKey, Method> mapping) {
        final String[] finalUrlPatterns = 0 != urlPatterns.length ? urlPatterns : DEFAULT_URL_PATTERNS;
        for (String urlPattern : finalUrlPatterns) {
            // 默认 handler "" 不需要添加.
            if (!DEFAULT_HANDLE_URL.equals(urlPattern) && !urlPattern.startsWith("/")) {
                urlPattern = "/" + urlPattern;
            }

            final MappingKey mappingKey = new MappingKey(httpMethod.toUpperCase(), urlPattern);
            if (mapping.containsKey(mappingKey)) {
                throw new IllegalStateException(String.format("urlPattern '%s' is already mapping", urlPattern));
            }
            mapping.put(mappingKey, method);
        }
    }
}
