package freework.web.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 下载视图.
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings("unused")
public abstract class DownloadView {
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * 下载的文件名称.
     */
    private String filename;

    /**
     * 下载内容的MIME类型
     */
    private String contentType;

    /**
     * 是否作为附件(非内联).
     */
    private boolean attachment;

    /**
     * 创建一个下载视图.
     *
     * @param filename 下载文件名称
     */
    public DownloadView(final String filename) {
        this(filename, true);
    }

    /**
     * 创建一个下载视图.
     *
     * @param filename    下载文件名称
     * @param contentType 下载内容的MIME类型
     */
    public DownloadView(final String filename, final String contentType) {
        this(filename, contentType, true);
    }

    /**
     * 创建一个下载视图.
     *
     * @param filename   文件名称
     * @param attachment 是否附件 (attachment/inline)
     */
    public DownloadView(String filename, boolean attachment) {
        this(filename, null, attachment);
    }

    /**
     * 创建一个下载视图.
     *
     * @param filename    文件名称
     * @param contentType 下载内容的MIME类型
     * @param attachment  是否附件 (attachment/inline)
     */
    public DownloadView(final String filename, final String contentType, final boolean attachment) {
        this.filename = filename;
        this.contentType = contentType;
        this.attachment = attachment;
    }

    /**
     * 获取下载文件名称.
     *
     * @return 文件名称
     */
    public String getFilename() {
        return filename;
    }

    /**
     * 设置下载文件名称.
     *
     * @param filename 文件名称
     */
    public void setFilename(final String filename) {
        this.filename = filename;
    }

    /**
     * 获取下载内容的 MIME 类型.
     *
     * @return MIME类型
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 设置下载内容的 MIME 类型.
     *
     * @param contentType MIME类型
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * 是否作为附件下载.
     *
     * @return 是否作为附件下载.
     */
    public boolean isAttachment() {
        return attachment;
    }

    /**
     * 设置是否作为附件下载.
     *
     * @param attachment 是否作为附件下载.
     */
    public void setAttachment(final boolean attachment) {
        this.attachment = attachment;
    }

    /* *************************
     *
     * *************************/

    /**
     * 对当前请求和响应执行下载操作.
     *
     * @param httpRequest  Http Request
     * @param httpResponse Http Response
     * @throws IOException 如果下载失败抛出
     */
    public void flow(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws IOException {
        if (!attachment) {
            /* inline */
            httpResponse.setHeader("Content-Disposition", "inline");
        } else {
            /*-
             * RFC2231:  parameter*=charset'lang'value (filename*=encoding''...):
             * 支持情况:
             * RFC2231:    ie 8, chrome 17, opera 11, firefox 11
             * ISO-8859-1: chrome, opera, firefox, safari
             * URLEncode:  IE6+, chrome
             * Base64:     chrome, firefox
             * 最主要的问题是 IE 和 Firefox 无法同时兼容
             */
            final StringBuilder buff = new StringBuilder("attachment; filename=");
            final String filename = null != this.filename
                    ? this.filename
                    : new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            /*
            // IE6, IE7
            if (request.getHeader("User-Agent").contains("MSIE")) {
                buff.append(URLEncoder.encode(filename, UTF_8.name()));
            } else {
                buff.append(new String(filename.getBytes(UTF_8), ISO_8859_1)).append("; ");
            }
            */
            /* 大部分浏览器已经支持 filename*=utf-8''filename, 因此的filename只考虑 IE */
            buff.append(URLEncoder.encode(filename, UTF8.name()));
            buff.append("; ");

            String rfc2231Filename = filename;
            try {
                rfc2231Filename = URLEncoder.encode(rfc2231Filename, UTF8.name());
            } catch (UnsupportedEncodingException ignore) { /*ignore*/ }

            buff.append("filename*=utf-8''").append(rfc2231Filename);

            httpResponse.setHeader("Content-Disposition", buff.toString());
        }

        if (StringUtils.isBlank(contentType) && null != httpRequest) {
            contentType = httpRequest.getSession().getServletContext().getMimeType(filename);
        }
        if (StringUtils.isBlank(contentType)) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        httpResponse.setContentType(contentType);

        try {
            this.doFlow(httpRequest, httpResponse);
        } catch (final IOException e) {
            if (!httpResponse.isCommitted()) {
                httpResponse.reset();
                httpResponse.setHeader("Content-Type", "text/html");
                httpResponse.setHeader("Content-Disposition", null);
                httpResponse.sendError(500);
            }
        }
    }

    /**
     * 将下载内容写入给定的响应.
     *
     * @param httpRequest  Http Request
     * @param httpResponse Http Response
     * @throws IOException 如果下载失败抛出该异常
     */
    protected abstract void doFlow(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws IOException;
}