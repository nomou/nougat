package freework.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class OncePerRequestFilter extends AbstractFilter {

    /**
     * Private internal log instance.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OncePerRequestFilter.class);

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
            LOG.trace("Filter '{}' already executed.  Proceeding without invoking this filter.", getFilterName());
            filterChain.doFilter(request, response);
        } else if (!isEnabled(request, response)) {
            LOG.debug("Filter '{}' is not enabled for the current request.  Proceeding without invoking this filter.", getFilterName());
            filterChain.doFilter(request, response);
        } else {
            // Do invoke this filter...
            LOG.trace("Filter '{}' not yet executed.  Executing now.", getFilterName());
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

    /**
     * Returns {@code true} if this filter should filter the specified request, {@code false} if it should let the
     * request/response pass through immediately to the next element in the {@code FilterChain}.
     * <p/>
     * This default implementation merely returns the value of {@link #isEnabled() isEnabled()}, which is
     * {@code true} by default (to ensure the filter always executes by default), but it can be overridden by
     * subclasses for request-specific behavior if necessary.  For example, a filter could be enabled or disabled
     * based on the request path being accessed.
     * <p/>
     * <b>Helpful Hint:</b> if your subclass extends {@link org.apache.shiro.web.filter.PathMatchingFilter PathMatchingFilter},
     * you may wish to instead override the
     * {@link org.apache.shiro.web.filter.PathMatchingFilter#isEnabled(ServletRequest, ServletResponse, String, Object)
     * PathMatchingFilter.isEnabled(request,response,path,pathSpecificConfig)}
     * method if you want to make your enable/disable decision based on any path-specific configuration.
     *
     * @param request the incoming servlet request
     * @param response the outbound servlet response
     * @return {@code true} if this filter should filter the specified request, {@code false} if it should let the
     * request/response pass through immediately to the next element in the {@code FilterChain}.
     * @throws IOException in the case of any IO error
     * @throws ServletException in the case of any error
     * @see org.apache.shiro.web.filter.PathMatchingFilter#isEnabled(ServletRequest, ServletResponse, String, Object)
     * @since 1.2
     */
    @SuppressWarnings({"UnusedParameters"})
    protected boolean isEnabled(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        return isEnabled();
    }

    /**
     * Return name of the request attribute that identifies that a request has already been filtered.
     * <p/>
     * The default implementation takes the configured {@link #getFilterName() name} and appends &quot;{@code .FILTERED}&quot;.
     * If the filter is not fully initialized, it falls back to the implementation's class name.
     *
     * @return the name of the request attribute that identifies that a request has already been filtered.
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
     * Same contract as for
     * {@link #doFilter(ServletRequest, ServletResponse, FilterChain)},
     * but guaranteed to be invoked only once per request.
     *
     * @param request  incoming {@code ServletRequest}
     * @param response outgoing {@code ServletResponse}
     * @param chain    the {@code FilterChain} to execute
     * @throws ServletException if there is a problem processing the request
     * @throws IOException      if there is an I/O problem processing the request
     */
    protected abstract void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException;
}
