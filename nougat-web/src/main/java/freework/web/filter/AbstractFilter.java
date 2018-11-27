package freework.web.filter;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Enumeration;

/**
 * 抽象 Filter 提供便捷获取 Filter 配置方法.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class AbstractFilter implements Filter {
    private transient FilterConfig config;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
        this.init();
    }

    public void init() throws ServletException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }

    /**
     * 获取当前Filter名称.
     *
     * @return 当前Filter名称
     */
    protected String getFilterName() {
        return getRequiredFilterConfig().getFilterName();
    }

    /**
     * 获取配置的Filter参数值.
     *
     * @param param 参数名称
     * @return 参数值
     */
    protected String getInitParam(final String param) {
        return getRequiredFilterConfig().getInitParameter(param);
    }

    /**
     * 获取所有配置的Filter参数名称.
     *
     * @return 参数名称
     */
    @SuppressWarnings("unchecked")
    public Enumeration<String> getInitParamNames() {
        return getRequiredFilterConfig().getInitParameterNames();
    }

    /**
     * 获取ServletContext.
     *
     * @return ServletContext
     */
    protected ServletContext getServletContext() {
        return getRequiredFilterConfig().getServletContext();
    }

    /**
     * 获取当前Filter配置.
     *
     * @return Filter配置
     */
    protected FilterConfig getFilterConfig() {
        return this.config;
    }

    /**
     * 获取必须的Filter配置, 如果没有初始化抛出{@link IllegalStateException}.
     *
     * @return Filter配置
     */
    private FilterConfig getRequiredFilterConfig() {
        final FilterConfig filterConfig = getFilterConfig();
        if (null == filterConfig) {
            throw new IllegalStateException("filter config not initialized");
        }
        return filterConfig;
    }
}
