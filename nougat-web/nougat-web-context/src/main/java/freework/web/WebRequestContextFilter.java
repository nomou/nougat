package freework.web;

import freework.web.filter.OncePerRequestFilter;
import freework.web.util.ThreadContext;
import freework.web.util.ThreadState;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * WebRequestContextFilter 用于设置 {@link WebRequestContext}
 *
 * @author vacoor
 * @see WebRequestContext
 */
public class WebRequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        WebRequestContextThreadState contextState = createContextState(httpRequest, httpResponse);

        try {
            if (null != contextState) {
                contextState.bind();
            }
            chain.doFilter(httpRequest, httpResponse);
        } finally {
            if (null != contextState) {
                contextState.restore();
            }
        }
    }

    @Override
    public void destroy() {
    }

    protected WebRequestContextThreadState createContextState(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return new WebRequestContextThreadState(httpRequest, httpResponse);
    }

    protected class WebRequestContextThreadState implements ThreadState {
        private Map<Object, Object> originalResources;
        private final HttpServletRequest httpRequest;
        private final HttpServletResponse httpResponse;
        private transient WebRequestContext requestContext;

        public WebRequestContextThreadState(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
            this.httpRequest = httpRequest;
            this.httpResponse = httpResponse;
        }

        @Override
        public void bind() {
            this.originalResources = ThreadContext.getResources();
            ThreadContext.remove();

            this.requestContext = WebRequestContext.begin(httpRequest, httpResponse, getServletContext());
        }

        @Override
        public void restore() {
            if (null != requestContext) {
                requestContext.end();
            }

            ThreadContext.remove();
            if (null != this.originalResources && !this.originalResources.isEmpty()) {
                ThreadContext.setResources(this.originalResources);
            }
        }

        @Override
        public void clear() {
            ThreadContext.remove();
        }

        public HttpServletRequest getHttpRequest() {
            return httpRequest;
        }

        public HttpServletResponse getHttpResponse() {
            return httpResponse;
        }

        public WebRequestContext getRequestContext() {
            return requestContext;
        }

    }
}
