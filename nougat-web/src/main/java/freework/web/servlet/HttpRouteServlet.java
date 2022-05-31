package freework.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 支持内部路由的Servlet.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class HttpRouteServlet extends HttpServlet {
    /**
     * 路由对象.
     */
    private HttpServletRouter router;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        router = HttpServletRouter.create(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        router = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        this.doRoute(httpRequest, httpResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        this.doRoute(httpRequest, httpResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPut(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        this.doRoute(httpRequest, httpResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDelete(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        this.doRoute(httpRequest, httpResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doHead(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        this.doRoute(httpRequest, httpResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doOptions(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        this.doRoute(httpRequest, httpResponse);
    }

    protected void doRoute(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        if (null == router) {
            throw new IllegalStateException("router is not initialized, the servlet is not initialized?");
        }
        router.route(httpRequest, httpResponse);
    }

    protected HttpServletRouter getRouter() {
        return router;
    }
}
