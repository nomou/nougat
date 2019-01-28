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
 * 内部资源 Servlet.
 *
 * @author vacoor
 */
public abstract class EmbeddedResourceServlet extends HttpServlet {
    public static final String JSP_WORK_PATH_IN_CONTEXT_PARAM = "jspWorkPathInContext";
    public static final String INIT_CLEANUP_JSP_WORK_PATH = "initCleanupJspWorkPath";
    public static final String DESTROY_CLEANUP_JSP_WORK_PATH = "destroyCleanupJspWorkPath";

    protected final String resourcePath;
    private String jspWorkPathInContext;

    public EmbeddedResourceServlet(final String resourcePath) {
        if (null == resourcePath) {
            throw new IllegalArgumentException("resourcePath must be not null");
        }
        this.resourcePath = !resourcePath.endsWith("/") ? resourcePath : resourcePath.substring(0, resourcePath.length() - 1);
        this.jspWorkPathInContext = "/WEB-INF/lib/__jsp_in_lib__";
    }

    @Override
    public void init() throws ServletException {
        super.init();

        final String pathInContext = getInitParameter(JSP_WORK_PATH_IN_CONTEXT_PARAM);
        String cleanup = getInitParameter(INIT_CLEANUP_JSP_WORK_PATH);
        cleanup = null != cleanup ? cleanup : "true";

        // jsp 工作目录
        if (null != pathInContext) {
            jspWorkPathInContext = pathInContext;
        }
        // 清理工作目录
        if (isTrue(cleanup)) {
            cleanupJspWorkPath();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        String cleanup = getInitParameter(DESTROY_CLEANUP_JSP_WORK_PATH);
        cleanup = null != cleanup ? cleanup : "true";
        if (isTrue(cleanup)) {
            cleanupJspWorkPath();
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String path = request.getPathInfo();

        contextPath = null != contextPath ? contextPath : "";
        if (path == null) {
            response.sendRedirect(contextPath + request.getServletPath() + "/");
            return;
        }

        if ("/".equals(path)) {
            handleRootRequest(request, response);
            return;
        }

        response.setCharacterEncoding("UTF-8");
        if (path.endsWith(".html")) {
            response.setContentType("text/html");
            writeResource(resourcePath + path, response);
        } else if (path.endsWith(".css")) {
            response.setContentType("text/css");
            writeResource(resourcePath + path, response);
        } else if (path.endsWith(".js")) {
            response.setContentType("text/javascript");
            writeResource(resourcePath + path, response);
        } else if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".gif") || path.endsWith(".ico")) {
            writeResource(resourcePath + path, response);
        } else if (path.endsWith(".swf")) {
            response.setContentType("application/x-shockwave-flash");
            writeResource(resourcePath + path, response);
        } else if (path.endsWith(".woff")) {
            response.setContentType("application/x-font-woff");
            writeResource(resourcePath + path, response);
        } else {
            handleRequest(path, request, response);
        }
    }

    /**
     * 直接将资源写入响应
     *
     * @param internalPath 资源内部路径
     * @param response     响应对象
     */
    protected void writeResource(String internalPath, HttpServletResponse response) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(internalPath);
        if (null != is) {
            try {
                int readed;
                byte[] buffer = new byte[1024];
                ServletOutputStream os = response.getOutputStream();

                while (-1 < (readed = is.read(buffer))) {
                    os.write(buffer, 0, readed);
                }
                os.flush();
            } finally {
                try {
                    is.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        } else {
            response.sendError(404);
        }
    }

    protected void handleRootRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("index.html");
    }

    /**
     * 处理一个非静态资源 请求
     *
     * @param path
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected abstract void handleRequest(String path, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    /**
     * forward 到一个内部 jsp
     *
     * @param jsp      内部 jsp 路径
     * @param request  请求对象
     * @param response 响应对象
     */
    protected void forwardInternalResource(String jsp, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        jsp = null != jsp ? jsp : "/index.jsp";
        jsp = jsp.startsWith("/") ? jsp : "/" + jsp;

        jspWorkPathInContext = null != jspWorkPathInContext ? jspWorkPathInContext : "/";
        jspWorkPathInContext = jspWorkPathInContext.startsWith("/") ? jspWorkPathInContext : "/" + jspWorkPathInContext;

        // 内部路径
        String pathInInternal = resourcePath + (!resourcePath.endsWith("/") ? jsp : jsp.substring(1));
        // 上下文路径
        String pathInContext = jspWorkPathInContext + (!jspWorkPathInContext.endsWith("/") ? jsp : jsp.substring(1));

        ServletContext context = getServletContext();
        URL resInContext = context.getResource(pathInContext);

        // 如果不存在资源, 释放内部 jsp 到 context path
        if (null == resInContext) {
            InputStream is = getInternalResourceAsStream(pathInInternal);
            if (null != is) {
                if (null == context.getRealPath(jspWorkPathInContext)) {
                    createWorkDir(jspWorkPathInContext);
                }
                File jspFile = new File(context.getRealPath(jspWorkPathInContext), jsp);
                File workDir = jspFile.getParentFile();
                if (!workDir.exists()) {
                    workDir.mkdirs();
                }

                IOUtils.flow(is, new FileOutputStream(jspFile), true, true);
                resInContext = context.getResource(pathInContext);
            }
        }

        if (null != resInContext) {
            request.getRequestDispatcher(pathInContext).forward(request, response);
        } else {
            response.sendError(404);
        }
    }

    protected InputStream getInternalResourceAsStream(String internalPath) {
        return getClass().getResourceAsStream(internalPath);
    }

    protected boolean isTrue(ServletRequest request, String paramName) {
        return isTrue(request.getParameter(paramName));
    }

    protected boolean isTrue(String value) {
        return ("true".equalsIgnoreCase(value) ||
                "t".equalsIgnoreCase(value) ||
                "1".equalsIgnoreCase(value) ||
                "enabled".equalsIgnoreCase(value) ||
                "y".equalsIgnoreCase(value) ||
                "yes".equalsIgnoreCase(value) ||
                "on".equalsIgnoreCase(value));
    }

    protected void cleanupJspWorkPath() {
        ServletContext context = getServletContext();
        String realPath = context.getRealPath(jspWorkPathInContext);
        if (null != realPath) {
            delete(new File(realPath));
        }
    }

    protected void createWorkDir(String pathInContext) {
        pathInContext = pathInContext.replace("\\", "/");
        pathInContext = pathInContext.startsWith("/") ? pathInContext.substring(1) : pathInContext;

        String[] paths = pathInContext.split("/");
        ServletContext context = getServletContext();

        String realPath;
        String path = "";
        String next;
        for (String p : paths) {
            next = path + "/" + p;
            realPath = context.getRealPath(next);
            if (null == realPath) {
                new File(context.getRealPath(path), p).mkdirs();
            }
            path = next;
        }
    }

    protected void delete(File fileOrDir) {
        if (null == fileOrDir || !fileOrDir.exists()) {
            return;
        }

        if (fileOrDir.isDirectory()) {
            File[] files = fileOrDir.listFiles();
            files = null != files ? files : new File[0];
            for (File file : files) {
                delete(file);
            }
            fileOrDir.delete();
        } else {
            fileOrDir.delete();
        }
    }
}
