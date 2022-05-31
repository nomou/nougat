package freework.web.filter;

import freework.web.util.WebUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import freework.web.ProxiedHttpRequest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
public class XSSDefenseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(final ServletRequest servletRequest,
                                    final ServletResponse servletResponse,
                                    final FilterChain chain) throws ServletException, IOException {
        ServletRequest request = servletRequest;
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            final HttpServletRequest httpRequest = WebUtils.toHttp(servletRequest);
            request = new XSSDefenseProxiedRequest(httpRequest);
        }

        chain.doFilter(request, servletResponse);
    }

    /**
     * XSS攻击防御处理.
     */
    private class XSSDefenseProxiedRequest extends ProxiedHttpRequest {

        /**
         * Create a proxied http request for http parameter.
         *
         * @param request HttpServletRequest.
         */
        XSSDefenseProxiedRequest(final HttpServletRequest request) {
            super(request);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String doGetProxiedHeaderValue(final String header, final String value) {
            return StringEscapeUtils.escapeHtml4(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String doGetProxiedParameterValue(final String param, final String value) {
            return StringEscapeUtils.escapeHtml4(value);
        }
    }
}
