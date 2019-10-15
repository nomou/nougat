package freework.web;

import freework.web.util.WebUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

/**
 */
public class WebContextListener implements ServletRequestListener {

    @Override
    public void requestInitialized(final ServletRequestEvent servletRequestEvent) {
        ServletRequest request = servletRequestEvent.getServletRequest();
        ServletContext context = servletRequestEvent.getServletContext();

        if (request instanceof HttpServletRequest) {
            WebRequestContext.begin(WebUtils.toHttp(request), null, context);
        }
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
        WebRequestContext context = WebRequestContext.getContext();
        if (null != context) {
            context.end();
        }
    }
}
