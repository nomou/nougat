package freework.web.servlet;

import freework.io.IOUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 嵌入资源 Servlet.
 *
 * <p>此类用于处理嵌入的html, image, jsp等资源.</p>
 *
 * @author vacoor
 */
public abstract class EmbeddedServlet extends HttpServlet {
    /**
     * 初始化参数: 嵌入jsp文件在context中的运行目录, 以"/WEB-INF"开头.
     */
    public static final String JSP_WORK_PATH_IN_CONTEXT_PARAM = "jspWorkPathInContext";

    /**
     * 初始化参数: 是否在初始化Servlet时清理"{@link #JSP_WORK_PATH_IN_CONTEXT_PARAM}"目录.
     */
    public static final String INIT_CLEANUP_JSP_WORK_PATH = "initCleanupJspWorkPath";

    /**
     * 初始化参数: 是否在销毁Servlet时清理"{@link #JSP_WORK_PATH_IN_CONTEXT_PARAM}"目录.
     */
    public static final String DESTROY_CLEANUP_JSP_WORK_PATH = "destroyCleanupJspWorkPath";

    /**
     * 默认的Jsp工作目录.
     */
    private static final String DEFAULT_JSP_WORK_PATH_IN_CONTEXT = "/WEB-INF/lib/__jsp_in_lib__";

    /**
     * "/".
     */
    private static final String SLASH = "/";

    /**
     * "true".
     */
    private static final String TRUE = "true";

    /**
     * 资源存放目录.
     */
    protected final String resourcePath;

    /**
     * Jsp文件工作目录.
     */
    private String jspWorkPathInContext;

    /**
     * 创建嵌入资源处理Servlet.
     *
     * @param resourcePath 资源存放目录
     */
    protected EmbeddedServlet(final String resourcePath) {
        if (null == resourcePath) {
            throw new IllegalArgumentException("resourcePath must be not null");
        }
        this.resourcePath = !resourcePath.endsWith(SLASH) ? resourcePath : resourcePath.substring(0, resourcePath.length() - 1);
        this.jspWorkPathInContext = DEFAULT_JSP_WORK_PATH_IN_CONTEXT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws ServletException {
        super.init();

        final String pathInContext = getInitParameter(JSP_WORK_PATH_IN_CONTEXT_PARAM);
        String cleanup = getInitParameter(INIT_CLEANUP_JSP_WORK_PATH);
        cleanup = null != cleanup ? cleanup : TRUE;

        // jsp 工作目录
        if (null != pathInContext) {
            jspWorkPathInContext = pathInContext;
        }

        // 清理工作目录
        if (isTrue(cleanup)) {
            cleanupJspWorkPath();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();

        String cleanup = getInitParameter(DESTROY_CLEANUP_JSP_WORK_PATH);
        cleanup = null != cleanup ? cleanup : TRUE;
        if (isTrue(cleanup)) {
            cleanupJspWorkPath();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void service(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        String contextPath = httpRequest.getContextPath();
        String path = httpRequest.getPathInfo();

        contextPath = null != contextPath ? contextPath : "";
        if (null == path) {
            // 映射 "/foo/*", 访问"/foo"时重定向到"/foo/"以便可以正确统一使用资源路径.
            httpResponse.sendRedirect(contextPath + httpRequest.getServletPath() + SLASH);
            return;
        }

        if (SLASH.equals(path)) {
            // 访问根目录 "/foo/".
            handleRootRequest(httpRequest, httpResponse);
            return;
        }

        httpResponse.setCharacterEncoding("UTF-8");
        if (path.endsWith(".html")) {
            httpResponse.setContentType("text/html");
            writeResource(resourcePath + path, httpResponse);
        } else if (path.endsWith(".css")) {
            httpResponse.setContentType("text/css");
            writeResource(resourcePath + path, httpResponse);
        } else if (path.endsWith(".js")) {
            httpResponse.setContentType("text/javascript");
            writeResource(resourcePath + path, httpResponse);
        } else if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".gif") || path.endsWith(".ico")) {
            writeResource(resourcePath + path, httpResponse);
        } else if (path.endsWith(".swf")) {
            httpResponse.setContentType("application/x-shockwave-flash");
            writeResource(resourcePath + path, httpResponse);
        } else if (path.endsWith(".woff")) {
            httpResponse.setContentType("application/x-font-woff");
            writeResource(resourcePath + path, httpResponse);
        } else {
            handleRequest(path, httpRequest, httpResponse);
        }
    }

    /**
     * 直接将资源写入响应.
     *
     * @param internalPath 资源内部路径
     * @param httpResponse Http响应对象
     * @throws IOException 如果输入/输出发生错误
     */
    protected void writeResource(final String internalPath, final HttpServletResponse httpResponse) throws IOException {
        final InputStream in = getClass().getClassLoader().getResourceAsStream(internalPath);
        if (null != in) {
            try {
                int readed;
                final byte[] buffer = new byte[1024];
                final ServletOutputStream out = httpResponse.getOutputStream();
                while (-1 < (readed = in.read(buffer))) {
                    out.write(buffer, 0, readed);
                }
                out.flush();
            } finally {
                try {
                    in.close();
                } catch (final IOException ignore) {
                    log("close embedded resource input stream error", ignore);
                }
            }
        } else {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 处理根目录访问.
     *
     * @param httpRequest  Http请求
     * @param httpResponse Http响应
     * @throws ServletException 如果请求处理失败
     * @throws IOException      如果输入/输出发生错误
     */
    protected void handleRootRequest(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        httpResponse.sendRedirect("index.html");
    }

    /**
     * 处理一个非静态资源 请求
     *
     * @param path         请求路径
     * @param httpRequest  Http请求
     * @param httpResponse Http响应
     * @throws ServletException 如果请求处理失败
     * @throws IOException      如果输入/输出发生错误
     */
    protected abstract void handleRequest(final String path, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException;

    /**
     * Forward 到一个内部 jsp
     *
     * @param jsp          内部 jsp 路径
     * @param httpRequest  Http请求
     * @param httpResponse Http响应
     * @throws ServletException 如果请求处理失败
     * @throws IOException      如果输入/输出发生错误
     */
    protected void forwardInternalJsp(String jsp, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws ServletException, IOException {
        jsp = null != jsp ? jsp : "/index.jsp";
        jsp = jsp.startsWith(SLASH) ? jsp : SLASH + jsp;

        jspWorkPathInContext = null != jspWorkPathInContext ? jspWorkPathInContext : SLASH;
        jspWorkPathInContext = jspWorkPathInContext.startsWith(SLASH) ? jspWorkPathInContext : SLASH + jspWorkPathInContext;

        // Jsp在工作目录的路径
        final String theJspWorkPathInContext = this.jspWorkPathInContext + (!this.jspWorkPathInContext.endsWith(SLASH) ? jsp : jsp.substring(1));

        final ServletContext context = getServletContext();

        // 如果工作目录不存在Jsp, 释放内部 Jsp 到工作目录.
        URL resourceInContext = context.getResource(theJspWorkPathInContext);
        if (null == resourceInContext) {
            // 内部资源路径
            final String pathInInternal = resourcePath + (!resourcePath.endsWith(SLASH) ? jsp : jsp.substring(1));
            final InputStream in = getInternalResourceAsStream(pathInInternal);
            if (null != in) {
                // 如果Jsp工作目录不存在, 创建工作目录
                if (null == context.getRealPath(theJspWorkPathInContext)) {
                    createJspWorkDirIfNecessary(theJspWorkPathInContext);
                }
                final File jspFile = new File(context.getRealPath(this.jspWorkPathInContext), jsp);
                final File jspParentDir = jspFile.getParentFile();
                if (!jspParentDir.exists() && !jspParentDir.mkdirs()) {
                    throw new IllegalStateException(String.format("can't release embedded JSP resource '%s' to work directory: '%s'", jsp, theJspWorkPathInContext));
                }

                IOUtils.flow(in, new FileOutputStream(jspFile), true, true);
                resourceInContext = context.getResource(theJspWorkPathInContext);
            }
        }

        if (null != resourceInContext) {
            httpRequest.getRequestDispatcher(theJspWorkPathInContext).forward(httpRequest, httpResponse);
        } else {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 获取内部资源输入流.
     *
     * @param internalPath 资源内部地址.
     * @return 如果资源存在返回输入流, 否则null
     */
    protected InputStream getInternalResourceAsStream(final String internalPath) {
        return getClass().getResourceAsStream(internalPath);
    }

    /**
     * 请求参数是否是逻辑True.
     *
     * @param request   Servlet请求
     * @param paramName 参数名称
     * @return 是否是逻辑True
     * @see #isTrue(String)
     */
    protected boolean isTrue(final ServletRequest request, final String paramName) {
        return isTrue(request.getParameter(paramName));
    }

    /**
     * 是否是逻辑True (true/t/1/enabled/y/yes/on).
     *
     * @param value 值
     * @return 是否逻辑True
     */
    protected boolean isTrue(final String value) {
        return ("true".equalsIgnoreCase(value) ||
                "t".equalsIgnoreCase(value) ||
                "1".equalsIgnoreCase(value) ||
                "enabled".equalsIgnoreCase(value) ||
                "y".equalsIgnoreCase(value) ||
                "yes".equalsIgnoreCase(value) ||
                "on".equalsIgnoreCase(value));
    }

    /**
     * 清理Jsp工作目录.
     */
    protected void cleanupJspWorkPath() {
        ServletContext context = getServletContext();
        String realPath = context.getRealPath(jspWorkPathInContext);
        if (null != realPath) {
            delete(new File(realPath));
        }
    }

    /**
     * 创建Jsp工作目录如果需要.
     *
     * @param pathInContext 工作路径
     */
    protected void createJspWorkDirIfNecessary(String pathInContext) {
        pathInContext = pathInContext.replace("\\", SLASH);
        pathInContext = pathInContext.startsWith(SLASH) ? pathInContext.substring(1) : pathInContext;

        final String[] paths = pathInContext.split(SLASH);
        final ServletContext context = getServletContext();

        String realPath;
        String path = "";
        String next;
        for (final String p : paths) {
            next = path + "/" + p;
            realPath = context.getRealPath(next);
            if (null == realPath) {
                final File jspWorkDir = new File(context.getRealPath(path), p);
                if (!jspWorkDir.mkdirs()) {
                    throw new IllegalStateException(String.format("can't create JSP work directory %s", jspWorkDir));
                }
            }
            path = next;
        }
    }

    /**
     * 删除给定文件或目录.
     *
     * @param fileOrDir 待删除的文件或目录
     */
    protected void delete(final File fileOrDir) {
        if (null == fileOrDir || !fileOrDir.exists()) {
            return;
        }

        if (fileOrDir.isDirectory()) {
            File[] files = fileOrDir.listFiles();
            files = null != files ? files : new File[0];
            for (final File file : files) {
                delete(file);
            }
        }
        if (!fileOrDir.delete()) {
            throw new IllegalStateException(String.format("can't delete %s", fileOrDir));
        }
    }
}
