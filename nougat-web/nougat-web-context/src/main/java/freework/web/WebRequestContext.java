package freework.web;

import freework.web.util.JaxElEngine;
import freework.web.util.ThreadContext;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Web 请求上下文
 * <p/>
 * 使用 ThreadLocal 实现, 因此在使用结束后必须调用 {@link #end()} 清理(Tomcat 等存在线程复用)
 *
 * @author vacoor
 */
@SuppressWarnings({"unchecked", "unused"})
public class WebRequestContext {
    private static final String THREAD_CONTEXT_KEY = WebRequestContext.class.getName() + ".THREAD_CONTEXT_KEY";

    private static final int INVALID_SCOPE = -1;
    public static final int CONTEXT_SCOPE = 0;
    public static final int REQUEST_SCOPE = 1;
    public static final int SESSION_SCOPE = 2;
    public static final int APPLICATION_SCOPE = 3;

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ServletContext servletContext;
    private final JaxElEngine elEngine;

    private WebRequestContext(final HttpServletRequest request, final HttpServletResponse response, final ServletContext servletContext) {
        this.request = assertNotNull(request, "request");
        this.response = assertNotNull(response, "response");
        this.servletContext = assertNotNull(servletContext, "servlet context");
        this.elEngine = new ContextElEngine(this);
    }

    private <T> T assertNotNull(T o, String name) {
        if (null == o) {
            throw new NullPointerException(name + " must be not null");
        }
        return o;
    }

    /* **********************************
     *        Holder methods
     * **********************************/

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public HttpSession getSession(boolean create) {
        return getRequest().getSession(create);
    }

    /* ****************************************
     *        find attribute method
     * ****************************************/


    public <T> T findAttribute(String name) {
        T value = getAttribute(name, CONTEXT_SCOPE);
        if (value != null) {
            return value;
        }

        value = getAttribute(name, REQUEST_SCOPE);
        if (value != null) {
            return value;
        }

        value = getAttribute(name, SESSION_SCOPE);
        if (value != null) {
            return value;
        }
        return getAttribute(name, APPLICATION_SCOPE);
    }


    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, int scope) {
        if (CONTEXT_SCOPE == scope) {
            return get(name);
        }
        if (REQUEST_SCOPE == scope) {
            return (T) getRequest().getAttribute(name);
        }
        if (SESSION_SCOPE == scope) {
            HttpSession session = getSession(false);
            return null != session ? (T) session.getAttribute(name) : null;
        }
        if (APPLICATION_SCOPE == scope) {
            return (T) getServletContext().getAttribute(name);
        }
        throw new IllegalArgumentException("Invalid scope " + scope);
    }


    public void setAttribute(String name, Object value, int scope) {
        if (CONTEXT_SCOPE == scope) {
            bind(name, value);
        } else if (REQUEST_SCOPE == scope) {
            getRequest().setAttribute(name, value);
        } else if (SESSION_SCOPE == scope) {
            getSession().setAttribute(name, value);
        } else if (APPLICATION_SCOPE == scope) {
            getServletContext().setAttribute(name, value);
        } else {
            throw new IllegalArgumentException("Invalid scope " + scope);
        }
    }


    public void removeAttribute(String name, int scope) {
        if (CONTEXT_SCOPE == scope) {
            unbind(name);
        } else if (REQUEST_SCOPE == scope) {
            getRequest().removeAttribute(name);
        } else if (SESSION_SCOPE == scope) {
            getSession().removeAttribute(name);
        } else if (APPLICATION_SCOPE == scope) {
            getServletContext().removeAttribute(name);
        } else {
            throw new IllegalArgumentException("Invalid scope " + scope);
        }
    }


    public int getAttributesScope(String name) {
        if (getAttribute(name, CONTEXT_SCOPE) != null) {
            return CONTEXT_SCOPE;
        }
        if (getAttribute(name, REQUEST_SCOPE) != null) {
            return REQUEST_SCOPE;
        }
        if (getAttribute(name, SESSION_SCOPE) != null) {
            return SESSION_SCOPE;
        }
        if (getAttribute(name, APPLICATION_SCOPE) != null) {
            return APPLICATION_SCOPE;
        }
        return INVALID_SCOPE;
    }


    @SuppressWarnings("unchecked")
    public Enumeration<String> getAttributeNamesInScope(int scope) {
        if (CONTEXT_SCOPE == scope) {
            return Collections.enumeration(getNames());
        }
        if (REQUEST_SCOPE == scope) {
            return getRequest().getAttributeNames();
        }
        if (SESSION_SCOPE == scope) {
            HttpSession session = getSession(false);
            return null != session ? session.getAttributeNames() : EMPTY_ENUM;
        }
        if (APPLICATION_SCOPE == scope) {
            return getServletContext().getAttributeNames();
        }
        throw new IllegalArgumentException("Invalid scope " + scope);
    }

    /* *****************************************
     *       expression resolveActualTypeArgs method
     * *****************************************/

    /**
     * resolveActualTypeArgs("name: ${param.name}")
     *
     * @param expression EL 表达式
     */
    public String resolve(String expression) {
        return elEngine.resolve(expression);
        // return elEngine.resolveActualTypeArgs("${" + expression + "}");
    }

    public <T> T resolve(String expression, Class<T> type) {
        return elEngine.resolve(expression, type);
        // return elEngine.resolveActualTypeArgs("${" + expression + "}", type);
    }

    /* ****************************************
     *
     * ****************************************/

    /**
     * 解除当前线程上下文绑定的 WebRequestContext
     */
    public void end() {
        unbind(THREAD_CONTEXT_KEY);
    }

    public static WebRequestContext begin(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        WebRequestContext wrc = new WebRequestContext(request, response, context);
        bind(THREAD_CONTEXT_KEY, wrc);
        return wrc;
    }

    /* **************************************************
     *
     * **************************************************/
    public static WebRequestContext getRequiredContext() throws IllegalStateException {
        WebRequestContext wrc = getContext();
        if (wrc == null) {
            throw new IllegalStateException("No WebRequestContext found: no WebRequestContextFilter config ?");
        }
        return wrc;
    }

    public static WebRequestContext getContext() {
        return get(THREAD_CONTEXT_KEY);
    }

    private static void bind(String key, Object value) {
        ThreadContext.put(key, value);
    }

    private static void unbind(String key) {
        ThreadContext.remove(key);
    }

    private static <T> T get(String key) {
        return (T) ThreadContext.get(key);
    }

    @SuppressWarnings("rawtypes")
    private static Set getNames() {
        return ThreadContext.getResources().keySet();
    }

    /* *********************
     *
     * *********************/

    private static final Enumeration<String> EMPTY_ENUM = new Enumeration<String>() {
        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public String nextElement() {
            throw new NoSuchElementException();
        }
    };

    private static class ContextElEngine extends JaxElEngine {
        ContextElEngine(final Object context) {
            super();
            Class<WebRequestContext> contextKey = WebRequestContext.class;
            this.resolver.add(new ImplicitObjectELResolver<WebRequestContext>(contextKey, new ContextImplicitObjectsFactory()));
            this.resolver.add(new ScopedAttributeELResolver());
            this.context.putContext(contextKey, context);
        }
    }


    //////////////////////////////////////////////////////////


    /* ****************************************************
     *                   EL Resolver
     * ************************************************** */


    private static class ContextImplicitObjectsFactory implements ImplicitObjectsFactory<WebRequestContext> {
        @Override
        public ImplicitObjectELResolver.ImplicitObjects<WebRequestContext> createImplicitObjects(final WebRequestContext context) {
            return new ImplicitObjectELResolver.ImplicitObjects<WebRequestContext>(context) {
                @Override
                protected HttpServletRequest getRequest(WebRequestContext context) {
                    return context.getRequest();
                }

                @Override
                protected ServletContext getServletContext(WebRequestContext context) {
                    return context.getServletContext();
                }

                @Override
                protected Map<String, Object> createContextScopeMap(final WebRequestContext context) {
                    return new EnumeratedMap<String, Object>() {
                        @Override
                        public Enumeration<String> enumerateKeys() {
                            return context.getAttributeNamesInScope(CONTEXT_SCOPE);
                        }

                        @Override
                        public boolean isMutable() {
                            return true;
                        }

                        @Override
                        public Object getValue(String pKey) {
                            return context.getAttribute(pKey, CONTEXT_SCOPE);
                        }
                    };
                }
            };
        }
    }

    /**
     * EL 作用于属性解析器
     *
     * @author vacoor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static class ScopedAttributeELResolver extends ELResolver {
        public static final int CONTEXT_SCOPE = WebRequestContext.CONTEXT_SCOPE;
        public static final int REQUEST_SCOPE = WebRequestContext.REQUEST_SCOPE;
        public static final int SESSION_SCOPE = WebRequestContext.SESSION_SCOPE;
        public static final int APPLICATION_SCOPE = WebRequestContext.APPLICATION_SCOPE;

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            if (null == context) {
                throw new NullPointerException();
            }
            if (null == base) {
                context.setPropertyResolved(true);
                if (property instanceof String) {
                    return getContext(context).findAttribute((String) property);
                }
            }
            return null;
        }

        @Override
        public Class<Object> getType(ELContext context, Object base, Object property) {
            if (null == context) {
                throw new NullPointerException();
            }
            if (null == base) {
                context.setPropertyResolved(true);
                return Object.class;
            }
            return null;
        }

        @Override
        public void setValue(ELContext context, Object base, Object property, Object val) {
            if (null == context) {
                throw new NullPointerException();
            }
            if (null == base) {
                context.setPropertyResolved(true);
                if (property instanceof String) {
                    String attr = (String) property;
                    WebRequestContext ctx = getContext(context);

                    if (ctx.getAttribute(attr, REQUEST_SCOPE) != null) {
                        ctx.setAttribute(attr, val, REQUEST_SCOPE);
                    } else if (ctx.getAttribute(attr, SESSION_SCOPE) != null) {
                        ctx.setAttribute(attr, val, SESSION_SCOPE);
                    } else if (ctx.getAttribute(attr, APPLICATION_SCOPE) != null) {
                        ctx.setAttribute(attr, val, APPLICATION_SCOPE);
                    } else {
                        ctx.setAttribute(attr, val, CONTEXT_SCOPE);
                    }
                }
            }
        }

        private WebRequestContext getContext(ELContext context) {
            return (WebRequestContext) context.getContext(WebRequestContext.class);
        }

        @Override
        public boolean isReadOnly(ELContext context, Object base, Object property) {
            if (context == null) {
                throw new NullPointerException();
            }
            if (base == null) {
                context.setPropertyResolved(true);
            }
            return false;
        }

        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
            Enumeration attrs;
            ArrayList<FeatureDescriptor> list = new ArrayList<FeatureDescriptor>();
            // WebRequestContext ctx = (WebRequestContext) context.getContext(JspContext.class);
            WebRequestContext ctx = getContext(context);

            attrs = ctx.getAttributeNamesInScope(CONTEXT_SCOPE);
            while (attrs.hasMoreElements()) {
                String name = (String) attrs.nextElement();
                Object value = ctx.getAttribute(name, CONTEXT_SCOPE);
                FeatureDescriptor descriptor = new FeatureDescriptor();
                descriptor.setName(name);
                descriptor.setDisplayName(name);
                descriptor.setShortDescription("context scope attribute");
                descriptor.setExpert(false);
                descriptor.setHidden(false);
                descriptor.setPreferred(true);
                descriptor.setValue("type", value.getClass());
                descriptor.setValue("resolvableAtDesignTime", Boolean.TRUE);
                list.add(descriptor);
            }

            attrs = ctx.getAttributeNamesInScope(REQUEST_SCOPE);
            while (attrs.hasMoreElements()) {
                String name = (String) attrs.nextElement();
                Object value = ctx.getAttribute(name, REQUEST_SCOPE);
                FeatureDescriptor descriptor = new FeatureDescriptor();
                descriptor.setName(name);
                descriptor.setDisplayName(name);
                descriptor.setShortDescription("request scope attribute");
                descriptor.setExpert(false);
                descriptor.setHidden(false);
                descriptor.setPreferred(true);
                descriptor.setValue("type", value.getClass());
                descriptor.setValue("resolvableAtDesignTime", Boolean.TRUE);
                list.add(descriptor);
            }

            attrs = ctx.getAttributeNamesInScope(SESSION_SCOPE);
            while (attrs.hasMoreElements()) {
                String name = (String) attrs.nextElement();
                Object value = ctx.getAttribute(name, SESSION_SCOPE);
                FeatureDescriptor descriptor = new FeatureDescriptor();
                descriptor.setName(name);
                descriptor.setDisplayName(name);
                descriptor.setShortDescription("session scope attribute");
                descriptor.setExpert(false);
                descriptor.setHidden(false);
                descriptor.setPreferred(true);
                descriptor.setValue("type", value.getClass());
                descriptor.setValue("resolvableAtDesignTime", Boolean.TRUE);
                list.add(descriptor);
            }

            attrs = ctx.getAttributeNamesInScope(APPLICATION_SCOPE);
            while (attrs.hasMoreElements()) {
                String name = (String) attrs.nextElement();
                Object value = ctx.getAttribute(name, APPLICATION_SCOPE);
                FeatureDescriptor descriptor = new FeatureDescriptor();
                descriptor.setName(name);
                descriptor.setDisplayName(name);
                descriptor.setShortDescription("application scope attribute");
                descriptor.setExpert(false);
                descriptor.setHidden(false);
                descriptor.setPreferred(true);
                descriptor.setValue("type", value.getClass());
                descriptor.setValue("resolvableAtDesignTime", Boolean.TRUE);
                list.add(descriptor);
            }
            return list.iterator();
        }

        @Override
        public Class<String> getCommonPropertyType(ELContext context, Object base) {
            return null == base ? String.class : null;
        }
    }


    public interface ImplicitObjectsFactory<Context> {

        ImplicitObjectELResolver.ImplicitObjects<Context> createImplicitObjects(Context context);

    }

    /**
     * EL 内置对象解析器
     * 修改自 {@link javax.servlet.jsp.el}
     *
     * @author vacoor
     */
    @SuppressWarnings({"unchecked", "rawtypes", "unused"})
    public static class ImplicitObjectELResolver<T> extends ELResolver {
        /**
         * EL 11 个 隐式对象
         * pageContext, pageScope,
         * requestScope, sessionScope, applicationScope
         * param, paramValues, header, headerValues
         * initParam
         * cookie
         * <p/>
         * 这里稍做修改
         */
        public static final String CONTEXT = "context";
        public static final String REQUEST = "request";
        public static final String SESSION = "session";
        public static final String APPLICATION = "application";
        public static final String PARAM = "param";
        public static final String PARAM_VALUES = "paramValues";
        public static final String HEADER = "header";
        public static final String HEADER_VALUES = "headerValues";
        public static final String COOKIE = "cookie";


        protected final Class<?> contextKey;
        protected final Class<?> contextType;
        protected final WebRequestContext.ImplicitObjectsFactory<T> implicitObjectsFactory;

        public ImplicitObjectELResolver(Class<T> contextKeyAndType, WebRequestContext.ImplicitObjectsFactory<T> implicitObjectsFactory) {
            this(contextKeyAndType, contextKeyAndType, implicitObjectsFactory);
        }

        public ImplicitObjectELResolver(Class<?> contextKey, Class<T> contextType, WebRequestContext.ImplicitObjectsFactory<T> implicitObjectsFactory) {
            this.contextKey = contextKey;
            this.contextType = contextType;
            this.implicitObjectsFactory = implicitObjectsFactory;
        }


        public Object getValue(ELContext context, Object base, Object property) {
            if (null == context) {
                throw new NullPointerException();
            }
            if (null != base) {
                return null;
            }
            ImplicitObjects<T> implicitObjects = (ImplicitObjects<T>) context.getContext(ImplicitObjects.class);
            if (null == implicitObjects) {
                T ctx = (T) context.getContext(contextKey);
                if (null == ctx) {
                    throw new IllegalStateException("can't found context for key: " + contextKey);
                }
                implicitObjects = implicitObjectsFactory.createImplicitObjects(ctx);
                context.putContext(ImplicitObjects.class, implicitObjects);
            }

            if (CONTEXT.equals(property)) {
                context.setPropertyResolved(true);
                // return ctx;
                return implicitObjects.getContextScopeMap();
            }
            if (REQUEST.equals(property)) {
                context.setPropertyResolved(true);
                return implicitObjects.getRequestScopeMap();
            }
            if (SESSION.equals(property)) {
                context.setPropertyResolved(true);
                return implicitObjects.getSessionScopeMap();
            }
            if (APPLICATION.equals(property)) {
                context.setPropertyResolved(true);
                return implicitObjects.getApplicationScopeMap();
            }
            if (PARAM.equals(property)) {
                context.setPropertyResolved(true);
                return implicitObjects.getParamMap();
            }
            if (PARAM_VALUES.equals(property)) {
                context.setPropertyResolved(true);
                return implicitObjects.getParamsMap();
            }
            if (HEADER.equals(property)) {
                context.setPropertyResolved(true);
                return implicitObjects.getHeaderMap();
            }
            if (HEADER_VALUES.equals(property)) {
                context.setPropertyResolved(true);
                return implicitObjects.getHeadersMap();
            }
            if (COOKIE.equals(property)) {
                context.setPropertyResolved(true);
                return implicitObjects.getCookieMap();
            }
            return null;
        }

        public Class<?> getType(ELContext context, Object base, Object property) {
            if (null == context) {
                throw new NullPointerException();
            }
            if ((null == base) && CONTEXT.equals(property) ||
                    REQUEST.equals(property) ||
                    SESSION.equals(property) ||
                    APPLICATION.equals(property) ||
                    PARAM.equals(property) ||
                    PARAM_VALUES.equals(property) ||
                    HEADER.equals(property) ||
                    HEADER_VALUES.equals(property) ||
                    COOKIE.equals(property)) {
                context.setPropertyResolved(true);
            }
            return null;
        }

        public void setValue(ELContext context, Object base, Object property, Object val) {
            if (null == context) {
                throw new NullPointerException();
            }
            if ((null == base) && CONTEXT.equals(property) ||
                    REQUEST.equals(property) ||
                    SESSION.equals(property) ||
                    APPLICATION.equals(property) ||
                    PARAM.equals(property) ||
                    PARAM_VALUES.equals(property) ||
                    HEADER.equals(property) ||
                    HEADER_VALUES.equals(property) ||
                    COOKIE.equals(property)) {
                throw new PropertyNotWritableException();
            }
        }

        public boolean isReadOnly(ELContext context, Object base, Object property) {
            if (null == context) {
                throw new NullPointerException();
            }
            if ((null == base) && CONTEXT.equals(property) ||
                    REQUEST.equals(property) ||
                    SESSION.equals(property) ||
                    APPLICATION.equals(property) ||
                    PARAM.equals(property) ||
                    PARAM_VALUES.equals(property) ||
                    HEADER.equals(property) ||
                    HEADER_VALUES.equals(property) ||
                    COOKIE.equals(property)) {
                context.setPropertyResolved(true);
                return true;
            }
            return false;    // Doesn't matter
        }

        public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
            return null;
        }

        public Class<String> getCommonPropertyType(ELContext context, Object base) {
            return null == base ? String.class : null;
        }


    /* *************************************************************
     *
     * *************************************************************/

        // XXX - I moved this class from commons-el to an inner class here
        // so that we do not have decode dependency from the JSP APIs into commons-el.
        // There might be decode better way to do this.

        /**
         * <p>This class is used to generate the implicit Map and List objects
         * that wrap various elements of the RequestContext.  It also returns the
         * correct implicit object for decode given implicit object name.
         *
         * @author Nathan Abramson - Art Technology Group
         */
        public abstract static class ImplicitObjects<Context> {
            static final String IMPLICIT_OBJECTS_ATTR = ImplicitObjects.class.getName();
            static final Enumeration<String> EMPTY_ENUM = new Enumeration<String>() {
                public boolean hasMoreElements() {
                    return false;
                }

                public String nextElement() {
                    throw new NoSuchElementException();
                }
            };

            Context context;
            Map<?, ?> mContext;
            Map<?, ?> mRequest;
            Map<?, ?> mSession;
            Map<?, ?> mApplication;
            Map<?, ?> mParam;
            Map<?, ?> mParams;
            Map<?, ?> mHeader;
            Map<?, ?> mHeaders;
            Map<?, ?> mInitParam;
            Map<?, ?> mCookie;

            protected ImplicitObjects(Context context) {
                this.context = context;
            }


            public Map<?, ?> getContextScopeMap() {
                return null != mContext ? mContext : (mContext = createContextScopeMap(context));
            }

            /**
             * Returns the Map that "wraps" request-scoped attributes
             */
            public Map<?, ?> getRequestScopeMap() {
                return null != mRequest ? mRequest : (mRequest = createRequestScopeMap(context));
            }

            /**
             * Returns the Map that "wraps" session-scoped attributes
             */
            public Map<?, ?> getSessionScopeMap() {
                return null != mSession ? mSession : (mSession = createSessionScopeMap(context));
            }

            /**
             * Returns the Map that "wraps" application-scoped attributes
             */
            public Map<?, ?> getApplicationScopeMap() {
                return null != mApplication ? mApplication : (mApplication = createApplicationScopeMap(context));
            }

            /**
             * Returns the Map that maps parameter name to decode single parameter
             * values.
             */
            public Map<?, ?> getParamMap() {
                return null != mParam ? mParam : (mParam = createParamMap(context));
            }

            /**
             * Returns the Map that maps parameter name to an array of parameter
             * values.
             */
            public Map getParamsMap() {
                return null != mParams ? mParams : (mParams = createParamsMap(context));
            }

            /**
             * Returns the Map that maps header name to decode single header
             * values.
             */
            public Map<?, ?> getHeaderMap() {
                return null != mHeader ? mHeader : (mHeader = createHeaderMap(context));
            }

            /**
             * Returns the Map that maps header name to an array of header
             * values.
             */
            public Map<?, ?> getHeadersMap() {
                return null != mHeaders ? mHeaders : (mHeaders = createHeadersMap(context));
            }

            /**
             * Returns the Map that maps cookie name to the first matching
             * Cookie in request.getCookies().
             */
            public Map<?, ?> getCookieMap() {
                return null != mCookie ? mCookie : (mCookie = createCookieMap(context));
            }


            protected abstract HttpServletRequest getRequest(Context context);

            protected abstract ServletContext getServletContext(Context context);

        /* **************************************
         *  Methods for generating wrapper maps
         * **************************************/

            protected abstract Map<String, Object> createContextScopeMap(Context context);

            protected Map<String, Object> createRequestScopeMap(final Context context) {
                return new EnumeratedMap<String, Object>() {
                    @Override
                    public Enumeration<String> enumerateKeys() {
                        HttpServletRequest request = getRequest(context);
                        return null == request ? EMPTY_ENUM : request.getAttributeNames();
                    }

                    @Override
                    public boolean isMutable() {
                        return true;
                    }

                    @Override
                    public Object getValue(String pKey) {
                        HttpServletRequest request = getRequest(context);
                        return null == request ? null : request.getAttribute(pKey);
                    }
                };
            }

            protected Map<String, Object> createSessionScopeMap(final Context context) {
                return new EnumeratedMap<String, Object>() {
                    @Override
                    public Enumeration<String> enumerateKeys() {
                        HttpServletRequest request = getRequest(context);
                        HttpSession session = null != request ? request.getSession(false) : null;
                        return null == session ? EMPTY_ENUM : session.getAttributeNames();
                    }

                    @Override
                    public boolean isMutable() {
                        return true;
                    }

                    @Override
                    public Object getValue(String pKey) {
                        HttpServletRequest request = getRequest(context);
                        HttpSession session = null != request ? request.getSession(false) : null;
                        return null == session ? null : session.getAttribute(pKey);
                    }
                };
            }

            protected Map<String, Object> createApplicationScopeMap(final Context context) {
                return new EnumeratedMap<String, Object>() {
                    @Override
                    public Enumeration<String> enumerateKeys() {
                        ServletContext appContext = getServletContext(context);
                        return null == appContext ? EMPTY_ENUM : appContext.getAttributeNames();
                    }

                    @Override
                    public boolean isMutable() {
                        return true;
                    }

                    @Override
                    public Object getValue(String pKey) {
                        ServletContext appContext = getServletContext(context);
                        return null == appContext ? null : appContext.getAttribute(pKey);
                    }
                };
            }


            /**
             * Creates the Map that maps parameter name to single parameter
             * value.
             */
            public Map<?, ?> createParamMap(final Context context) {
                return new EnumeratedMap<Object, Object>() {
                    @Override
                    public Enumeration enumerateKeys() {
                        HttpServletRequest request = getRequest(context);
                        return null == request ? EMPTY_ENUM : request.getParameterNames();
                    }

                    @Override
                    public Object getValue(Object pKey) {
                        HttpServletRequest request = getRequest(context);
                        return null != request && (pKey instanceof String) ? request.getParameter((String) pKey) : null;
                    }

                    @Override
                    public boolean isMutable() {
                        return false;
                    }
                };
            }

            /**
             * Creates the Map that maps parameter name to an array of parameter
             * values.
             */
            public Map<?, ?> createParamsMap(final Context context) {
                return new EnumeratedMap<Object, Object>() {
                    @Override
                    public Enumeration enumerateKeys() {
                        HttpServletRequest request = getRequest(context);
                        return null == request ? EMPTY_ENUM : request.getParameterNames();
                    }

                    @Override
                    public Object getValue(Object pKey) {
                        HttpServletRequest request = getRequest(context);
                        return null != request && (pKey instanceof String) ? request.getParameterValues((String) pKey) : null;
                    }

                    @Override
                    public boolean isMutable() {
                        return false;
                    }
                };
            }

            /**
             * Creates the Map that maps header name to single header
             * value.
             */
            public Map<?, ?> createHeaderMap(final Context context) {
                return new EnumeratedMap<Object, Object>() {
                    @Override
                    public Enumeration enumerateKeys() {
                        HttpServletRequest request = getRequest(context);
                        return null == request ? EMPTY_ENUM : request.getHeaderNames();
                    }

                    @Override
                    public Object getValue(Object pKey) {
                        HttpServletRequest request = getRequest(context);
                        return null != request && (pKey instanceof String) ? request.getHeader((String) pKey) : null;
                    }

                    @Override
                    public boolean isMutable() {
                        return false;
                    }
                };
            }

            //-------------------------------------

            /**
             * Creates the Map that maps header name to an array of header
             * values.
             */
            public Map<?, ?> createHeadersMap(final Context context) {
                return new EnumeratedMap<Object, Object>() {
                    @Override
                    public Enumeration enumerateKeys() {
                        HttpServletRequest request = getRequest(context);
                        return null == request ? EMPTY_ENUM : request.getHeaderNames();
                    }

                    @Override
                    public Object getValue(Object pKey) {
                        HttpServletRequest request = getRequest(context);
                        if (null != request && pKey instanceof String) {
                            // Drain the header enumeration
                            List<Object> l = new ArrayList<Object>();
                            Enumeration e = request.getHeaders((String) pKey);
                            if (e != null) {
                                while (e.hasMoreElements()) {
                                    l.add(e.nextElement());
                                }
                            }
                            return l.toArray(new String[l.size()]);
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public boolean isMutable() {
                        return false;
                    }
                };
            }

            /**
             * Creates the Map that maps init parameter name to single init
             * parameter value.
             */
            public Map<?, ?> createInitParamMap(final Context pContext) {
                return new EnumeratedMap<Object, Object>() {
                    @Override
                    public Enumeration enumerateKeys() {
                        ServletContext context = getServletContext(pContext);
                        return null == context ? EMPTY_ENUM : context.getInitParameterNames();
                    }

                    @Override
                    public Object getValue(Object pKey) {
                        ServletContext context = getServletContext(pContext);
                        return null != context && (pKey instanceof String) ? context.getInitParameter((String) pKey) : null;
                    }

                    @Override
                    public boolean isMutable() {
                        return false;
                    }
                };
            }

            /**
             * Creates the Map that maps cookie name to the first matching
             * Cookie in request.getCookies().
             */
            public Map<String, Cookie> createCookieMap(final Context context) {
                // Read all the cookies and construct the entire map
                HttpServletRequest request = getRequest(context);
                if (null == request) {
                    return Collections.emptyMap();
                }
                Cookie[] cookies = request.getCookies();
                Map<String, Cookie> ret = new HashMap<String, Cookie>();
                for (int i = 0; cookies != null && i < cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    if (cookie != null) {
                        String name = cookie.getName();
                        if (!ret.containsKey(name)) {
                            ret.put(name, cookie);
                        }
                    }
                }
                return ret;
            }
        }
    }



    /* **********************************
     *
     * **********************************/

    // XXX - I moved this class from commons-el to an inner class here
    // so that we do not have decode dependency from the JSP APIs into commons-el.
    // There might be decode better way to do this.

    /**
     * <p>This is decode Map implementation driven by decode data source that only
     * provides an enumeration of keys and decode getValue(key) method.  This
     * class must be subclassed to implement those methods.
     * <p/>
     * <p>Some of the methods may incur decode performance penalty that
     * involves enumerating the entire data source.  In these cases, the
     * Map will try to save the results of that enumeration, but only if
     * the underlying data source is immutable.
     *
     * @author Nathan Abramson - Art Technology Group
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public abstract static class EnumeratedMap<K, V> implements Map<K, V> {
        Map<K, V> mMap;

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object pKey) {
            return null != get(pKey);
        }

        @Override
        public boolean containsValue(Object pValue) {
            return getAsMap().containsValue(pValue);
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return getAsMap().entrySet();
        }

        @Override
        public V get(Object pKey) {
            return getValue((K) pKey);
        }

        @Override
        public boolean isEmpty() {
            return !enumerateKeys().hasMoreElements();
        }

        @Override
        public Set<K> keySet() {
            return getAsMap().keySet();
        }

        @Override
        public V put(K pKey, V pValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> pMap) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object pKey) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return getAsMap().size();
        }

        @Override
        public Collection<V> values() {
            return getAsMap().values();
        }

        /* *************************************
         *         Abstract methods
         * *************************************/

        /**
         * Returns an enumeration of the keys
         */
        public abstract Enumeration<K> enumerateKeys();

        /**
         * Returns true if it is possible for this data source to change
         */
        public abstract boolean isMutable();

        /**
         * Returns the value associated with the given key, or null if not
         * found.
         */
        public abstract V getValue(K pKey);

        /* ***************************************
         *
         * ***************************************/

        /**
         * Converts the MapSource to decode Map.  If the map is not mutable, this
         * is cached
         */
        public Map<K, V> getAsMap() {
            if (mMap != null) {
                return mMap;
            } else {
                Map<K, V> m = convertToMap();
                if (!isMutable()) {
                    mMap = m;
                }
                return m;
            }
        }

        /**
         * Converts to decode Map
         */
        Map<K, V> convertToMap() {
            Map<K, V> ret = new HashMap<K, V>();
            for (Enumeration<K> e = enumerateKeys(); e.hasMoreElements(); ) {
                K key = e.nextElement();
                V value = getValue(key);
                ret.put(key, value);
            }
            return ret;
        }
    }
}
