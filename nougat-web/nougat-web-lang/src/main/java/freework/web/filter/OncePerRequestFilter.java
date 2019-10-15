package freework.web.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Filter base class that guarantees to be just executed once per request,
 * on any servlet container. It provides a {@link #doFilterInternal}
 * method with HttpServletRequest and HttpServletResponse arguments.
 * <p/>
 * The {@link #getAlreadyFilteredAttributeName} method determines how
 * to identify that a request is already filtered. The default implementation
 * is based on the configured name of the concrete filter instance.
 * <h3>Controlling filter execution</h3>
 * 1.2 introduced the {@link #isEnabled(ServletRequest, ServletResponse)} method and
 * {@link #isEnabled()} property to allow explicit controll over whether the filter executes (or allows passthrough)
 * for any given request.
 * <p/>
 * <b>NOTE</b> This class was initially borrowed from the Spring framework but has continued modifications.
 *
 * @since 0.1
 */
@Slf4j
public abstract class OncePerRequestFilter extends AbstractFilter {
    /**
     * Suffix that gets appended to the filter name for the "already filtered" request attribute.
     *
     * @see #getAlreadyFilteredAttributeName
     */
    public static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";

    /**
     * Determines generally if this filter should execute or let requests fall through to the next chain element.
     *
     * @see #isEnabled()
     */
    private boolean enabled = true; //most filters wish to execute when configured, so default to true

    /**
     * Returns {@code true} if this filter should <em>generally</em><b>*</b> execute for any request,
     * {@code false} if it should let the request/response pass through immediately to the next
     * element in the {@link FilterChain}.  The default value is {@code true}, as most filters would inherently need
     * to execute when configured.
     * <p/>
     * <b>*</b> This configuration property is for general configuration for any request that comes through
     * the filter.  The
     * {@link #isEnabled(ServletRequest, ServletResponse) isEnabled(request,response)}
     * method actually determines whether or not if the filter is enabled based on the current request.
     *
     * @return {@code true} if this filter should <em>generally</em> execute, {@code false} if it should let the
     * request/response pass through immediately to the next element in the {@link FilterChain}.
     * @since 1.2
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether or not this filter <em>generally</em> executes for any request.  See the
     * {@link #isEnabled() isEnabled()} JavaDoc as to what <em>general</em> execution means.
     *
     * @param enabled whether or not this filter <em>generally</em> executes.
     * @since 1.2
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * This {@code doFilter} implementation stores a request attribute for
     * "already filtered", proceeding without filtering again if the
     * attribute is already there.
     *
     * @see #getAlreadyFilteredAttributeName
     * @see #doFilterInternal
     */
    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String alreadyFilteredAttributeName = getAlreadyFilteredAttributeName();
        if (null != request.getAttribute(alreadyFilteredAttributeName)) {
            if (log.isTraceEnabled()) {
                log.trace("Filter '{}' already executed.  Proceeding without invoking this filter.", getFilterName());
            }
            filterChain.doFilter(request, response);
        } else if (!isEnabled(request, response)) {
            if (log.isDebugEnabled()) {
                log.debug("Filter '{}' is not enabled for the current request.  Proceeding without invoking this filter.", getFilterName());
            }
            filterChain.doFilter(request, response);
        } else {
            // Do invoke this filter...
            if (log.isTraceEnabled()) {
                log.trace("Filter '{}' not yet executed.  Executing now.", getFilterName());
            }
            request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE);

            try {
                doFilterInternal(request, response, filterChain);
            } finally {
                // Once the request has finished, we're done and we don't
                // need to mark as 'already filtered' any more.
                request.removeAttribute(alreadyFilteredAttributeName);
            }
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    protected boolean isEnabled(final ServletRequest request, final ServletResponse response) throws ServletException, IOException {
        return isEnabled();
    }

    /**
     * 获取已过滤的属性标识.
     *
     * @return 已过滤属性标识.
     * @see #getFilterName
     * @see #ALREADY_FILTERED_SUFFIX
     */
    protected String getAlreadyFilteredAttributeName() {
        String name = getFilterName();
        if (null == name) {
            name = getClass().getName();
        }
        return name + ALREADY_FILTERED_SUFFIX;
    }

    /**
     * 执行内部过滤逻辑.
     *
     * @param httpRequest  http请求
     * @param httpResponse http响应
     * @param chain        过滤器链
     * @throws ServletException 如果处理请求发生错误
     * @throws IOException      如果发生IO错误
     */
    protected abstract void doFilterInternal(final ServletRequest httpRequest, final ServletResponse httpResponse, final FilterChain chain) throws ServletException, IOException;

}
