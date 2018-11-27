package freework.web.filter;

import freework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
public class GzipFilter extends OncePerRequestFilter {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private int buffSize;

    @Override
    public void init() throws ServletException {
        int size = DEFAULT_BUFFER_SIZE;
        String buff = getInitParam("buff");
        if (null != buff) {
            size = Integer.valueOf(buff);
        }
        this.buffSize = size;
    }

    @Override
    protected void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        if (!WebUtils.isGzipSupported(httpRequest) || response instanceof GzipProxiedResponse) {
            chain.doFilter(request, response);
            return;
        }

        int buff = 1 > this.buffSize ? this.buffSize : DEFAULT_BUFFER_SIZE;
        GzipProxiedResponse gzipHttpResponse = new GzipProxiedResponse(httpResponse, buff);
        try {
            chain.doFilter(request, gzipHttpResponse);
        } finally {
            gzipHttpResponse.finish();
        }
    }
}
