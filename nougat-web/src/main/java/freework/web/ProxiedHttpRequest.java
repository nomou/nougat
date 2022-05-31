package freework.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求参数代理请求, 该类用于根据给定策略对请求参数进行处理.
 *
 * @author vacoor
 * @version 1.0
 * @since 1.0
 */
public abstract class ProxiedHttpRequest extends HttpServletRequestWrapper {
    private Map<String, String[]> udfParamsMap;

    /**
     * Create a proxied http request for http parameter.
     *
     * @param request HttpServletRequest.
     */
    public ProxiedHttpRequest(final HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getHeader(final String name) {
        return doGetProxiedHeaderValue(name, super.getHeader(name));
    }

    @Override
    public Enumeration<String> getHeaders(final String name) {
        return new Enumeration<String>() {
            @SuppressWarnings("unchecked")
            final Enumeration<String> values = ProxiedHttpRequest.super.getHeaders(name);

            @Override
            public boolean hasMoreElements() {
                return values.hasMoreElements();
            }

            @Override
            public String nextElement() {
                return doGetProxiedHeaderValue(name, values.nextElement());
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueryString() {
        return super.getQueryString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameter(final String name) {
        final String[] values = getParameterValues(name);
        return null == values || 1 > values.length ? null : values[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getParameterValues(final String name) {
        return getParameterMap().get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String[]> getParameterMap() {
        if (null == udfParamsMap) {
            final Map<String, String[]> paramsMap = super.getParameterMap();
            final Map<String, String[]> cleanedMap = new LinkedHashMap<String, String[]>();

            // clean parameter values.
            for (final Map.Entry<String, String[]> entry : paramsMap.entrySet()) {
                final String key = entry.getKey();
                final String[] values = entry.getValue();
                final String[] cleaned = new String[values.length];

                for (int i = 0; i < values.length; i++) {
                    cleaned[i] = doGetProxiedParameterValue(key, values[i]);
                }
                cleanedMap.put(key, cleaned);
            }

            udfParamsMap = Collections.unmodifiableMap(cleanedMap);
        }
        return udfParamsMap;
    }

    /**
     * 获取请求头代理后的值.
     *
     * @param header 请求头.
     * @param value  请求头原始值.
     * @return 代理后的值.
     */
    protected String doGetProxiedHeaderValue(final String header, final String value) {
        return value;
    }

    /**
     * 获取参数对应值代理后的值.
     *
     * @param param 参数名称。
     * @param value 参数值.
     * @return 代理后的值.
     */
    protected String doGetProxiedParameterValue(final String param, final String value) {
        return value;
    }
}
