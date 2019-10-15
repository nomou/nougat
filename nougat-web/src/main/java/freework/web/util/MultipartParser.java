package freework.web.util;

import freework.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Multipart 请求工具类.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class MultipartParser {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartParser.class);

    protected static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";
    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String CONTENT_DISPOSITION = "Content-Disposition";
    protected static final String MULTIPART_PREFIX = "multipart/";

    protected static final byte CR = '\r';
    protected static final byte LF = '\n';
    protected static final byte DASH = '-';
    protected static final byte COLON = ':';
    protected static final byte[] FIELD_SEPARATOR = {CR, LF};
    protected static final byte[] STREAM_TERMINATOR = {DASH, DASH};
    protected static final byte[] HEADER_SEPARATOR = {CR, LF, CR, LF};

    public interface Handler {

        void onParameter(String name, String value, Map<String, String> multipartHeaders);

        void onPart(String name, String filename, InputStream multipartIn, Map<String, String> multipartHeaders) throws IOException;

    }

    /**
     * 是否是 Multipart 请求
     *
     * @param request HttpServletRequest
     */
    public static boolean isMultipartRequest(HttpServletRequest request) {
        return WebUtils.isMultipartRequest(request);
    }

    /**
     * 解析给定的 Multipart 请求
     *
     * @param httpRequest Multipart 请求
     * @param handler     Multipart 处理器
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static void parse(final HttpServletRequest httpRequest, final Handler handler) throws IOException {
        LOG.debug("parse multipart request: {}", httpRequest);

        // spring web mvc
        if (null != SPRING_MULTIPART && SPRING_MULTIPART.isInstance(httpRequest)) {
            doHandle(httpRequest, handler);
        } else {
            doParse(httpRequest, handler);
        }
    }

    static void doHandle(final HttpServletRequest httpRequest, final Handler handler) throws IOException {
        final MultipartHttpServletRequest request = MultipartHttpServletRequest.class.cast(httpRequest);
        final Map<String, MultipartFile> fileMap = request.getFileMap();
        final Set<Map.Entry<String, String[]>> set = request.getParameterMap().entrySet();
        for (Map.Entry<String, String[]> entry : set) {
            final String name = entry.getKey();
            for (final String value : entry.getValue()) {
                handler.onParameter(name, value, request.getMultipartHeaders(name).toSingleValueMap());
            }
        }
        for (final Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            final String name = entry.getKey();
            final MultipartFile value = entry.getValue();
            final InputStream in = value.getInputStream();
            try {
                handler.onPart(name, value.getOriginalFilename(), in, request.getMultipartHeaders(name).toSingleValueMap());
            } finally {
                IOUtils.close(in);
            }
        }
    }

    /*-
     * \r\n--boundary\r\n
     * multipart header\r\n
     * \r\n
     * multipart body
     * \r\n--boundary\r\n
     * ....
     * \r\n--boundary--
     */
    static void doParse(HttpServletRequest request, Handler handler) throws IOException {
        String ctype = request.getContentType();
        String headerCtype = request.getHeader(CONTENT_TYPE);
        if (null == ctype) {
            ctype = headerCtype;
        } else if (null != headerCtype) {
            ctype = ctype.length() > headerCtype.length() ? ctype : headerCtype;
        }
        if (null == ctype || !ctype.toLowerCase().startsWith(MULTIPART_PREFIX)) {
            throw new IllegalArgumentException("the request was rejected because not multipart request");
        }

        // multipart/form-data; boundary=----WebKitFormBoundaryDJetQ8AxH0xv9wkh
        final byte[] boundary = determineBoundary(ctype);
        final int length = request.getContentLength();
        String charset = determineEncoding(ctype);
        charset = null != charset ? charset : request.getCharacterEncoding();
        charset = null != charset ? charset : DEFAULT_CHARACTER_ENCODING;

        LOG.debug("parse multipart request, Content-Type: {}, Content-Length: {}, Charset: {}", ctype, length, charset);

        if (boundary == null) {
            throw new IllegalArgumentException("the request was rejected because no multipart boundary was found");
        }

        // start
        final InputStream input = request.getInputStream();
        if (next(input, boundary, true)) {
            do {
                doParsePart(input, boundary, charset, handler);
            } while (next(input, boundary, false));
            LOG.debug("parse over");
        } else {
            LOG.debug("no multipart content");
        }
    }


    /**
     * 输入流是否有下一个 Multipart
     *
     * @param in           输入流
     * @param boundary     boundary
     * @param readBoundary 是否读取 boundary
     * @return 是否存在下一个 Multipart
     * @throws IOException
     */
    private static boolean next(InputStream in, byte[] boundary, boolean readBoundary) throws IOException {
        if (readBoundary) {
            byte[] bytes = new byte[boundary.length];
            int len = in.read(bytes);
            if (!safeEquals(bytes, 0, len, boundary)) {
                throw new IOException("Not matched boundary");
            }
        }

        byte[] marker = new byte[2];
        marker[0] = (byte) in.read();

        if (LF == marker[0]) {
            // Work around IE5 Mac bug with input type=image.
            // Because the boundary delimiter, not including the trailing
            // CRLF, must not appear within any file (RFC 2046, section
            // 5.1.1), we know the missing CR is due to a buggy browser
            // rather than a file containing something similar to a
            // boundary.
            return true;
        }

        boolean nextChunk;
        marker[1] = (byte) in.read();
        if (Arrays.equals(marker, FIELD_SEPARATOR)) {
            nextChunk = true;
        } else if (Arrays.equals(marker, STREAM_TERMINATOR)) {
            nextChunk = false;
        } else {
            throw new IOException("Unexpected characters follow a boundary");
        }
        return nextChunk;
    }

    /**
     * 解析 Multipart
     *
     * @param in             Multipart 输入流
     * @param boundary       boundary
     * @param defaultCharset 默认编码
     * @param handler
     * @throws IOException
     */
    private static void doParsePart(InputStream in, byte[] boundary, String defaultCharset, Handler handler) throws IOException {
        Map<String, String> headers = doParsePartHeaders(in, defaultCharset, false);
        String ctype = headers.get(CONTENT_TYPE);
        String cdl = headers.get(CONTENT_DISPOSITION);
        String name = getName(cdl);
        String filename = getFileName(cdl);
        String charset = determineEncoding(ctype);
        charset = null != charset ? charset : defaultCharset;

        LOG.debug("start parse part: {}", headers);

        PartBodyInputStream pin = new PartBodyInputStream(in, boundary);
        try {
            if (null != handler) {
                // form field
                if (null == filename) {
                    String value = IOUtils.toString(pin, Charset.forName(charset), true);
                    handler.onParameter(name, value, headers);
                } else {
                    handler.onPart(name, filename, pin, headers);
                }
            }
        } finally {
            IOUtils.close(pin);
            LOG.debug("end parse part: {}", headers);
        }
    }

    /**
     * 解析 Multipart 头信息
     *
     * @param in      Multipart 输入流
     * @param charset 编码
     * @return 头信息 Map
     * @throws IOException
     */
    private static Hashtable<String, String> doParsePartHeaders(InputStream in, String charset, boolean lowercase) throws IOException {
        Hashtable<String, String> headers = new Hashtable<String, String>();
        ByteArrayOutputStream headerBuf = new ByteArrayOutputStream();
        String headerName = null;
        byte[] buf = new byte[HEADER_SEPARATOR.length];
        for (int i = 0; i < HEADER_SEPARATOR.length; ) {
            /*
            if(0 > (buf[i] = (byte) in.read())) {
                throw new IllegalStateException("EOF ");
            }
            */
            buf[i] = (byte) (in.read() & 0xff);
            if (buf[i] == HEADER_SEPARATOR[i]) {
                i++;
            } else {
                if (COLON == buf[i]) {
                    if (null == headerName) {
                        headerName = headerBuf.toString(charset);
                        headerBuf.reset();
                    }
                } else if (1 < i && buf[i - 2] == CR && buf[i - 1] == LF) {
                    if (null != headerName) {
                        headerName = lowercase ? headerName.toLowerCase() : headerName;
                        headers.put(headerName.trim(), headerBuf.toString(charset).trim());
                        headerBuf.reset();
                        headerName = null;
                    } else {
                        throw new IllegalStateException("header name is not found");
                    }
                    headerBuf.write(buf, i, 1);
                } else {
                    headerBuf.write(buf, 0, i + 1);
                }
                i = 0;
            }
        }
        if (null != headerName) {
            headerName = lowercase ? headerName.toLowerCase() : headerName;
            headers.put(headerName.trim(), headerBuf.toString(charset).trim());
        }
        return headers;
    }

    private static class PartBodyInputStream extends InputStream {
        private final InputStream in;
        private final byte[] boundary;
        private final byte[] buf;
        private int start;
        private int end;
        private boolean eof;

        public PartBodyInputStream(InputStream in, byte[] boundary) {
            this.in = in;
            this.boundary = new byte[boundary.length + 2];
            System.arraycopy(boundary, 0, this.boundary, 2, boundary.length);
            this.boundary[0] = CR;
            this.boundary[1] = LF;
            this.buf = new byte[this.boundary.length];
        }

        @Override
        public synchronized int read() throws IOException {
            if (eof) {
                return -1;
            }

            if (start < end && buf[start] != boundary[0]) {
                return buf[start++] & 0xff;
            }

            for (int i = 0; i < boundary.length; ) {
                while (start + i < end && buf[start + i] == boundary[i]) {
                    i++;
                }
                // 不是全部匹配
                if (start + i < end || end - start >= boundary.length) {
                    break;
                }

                // 没有空位
                if (end >= boundary.length) {
                    System.arraycopy(buf, start, buf, 0, end - start);
                    end -= start;
                    start = 0;
                }

                buf[start + i] = (byte) in.read();
                if (buf[start + i] == boundary[i]) {
                    i++;
                    end = start + i;
                } else {
                    end = start + i + 1;
                    break;
                }
            }

            if (end - start >= boundary.length) {
                eof = true;
                return -1;
            }
            return buf[start++] & 0xff;
        }

        @Override
        public synchronized void close() throws IOException {
            if (!eof) {
                while (-1 != read()) {
                    // SKIP TO END
                }
            }
            super.close();
        }
    }

    private static byte[] determineBoundary(String ctype) {
        // Use lastIndexOf() because IE 4.01 on Win98 has been known to send the
        // "boundary=" string multiple times.  Thanks to David Wall for this fix.
        int index = ctype.lastIndexOf("boundary=");
        if (index == -1) {
            return null;
        }
        String boundary = ctype.substring(index + 9);  // 9 for "boundary="
        if (boundary.charAt(0) == '"') {
            // The boundary is enclosed in quotes, strip them
            index = boundary.lastIndexOf('"');
            boundary = boundary.substring(1, index);
        }

        // The real boundary is always preceeded by an extra "--"
        boundary = "--" + boundary;

        byte[] bytes = null;
        try {
            bytes = boundary.getBytes(DEFAULT_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            bytes = boundary.getBytes();
        }

        return bytes;
    }

    private static String determineEncoding(String ctype) {
        String charset = null;
        if (ctype != null) {
            ctype = ctype.trim().toLowerCase(Locale.ENGLISH);
            int index = ctype.lastIndexOf("charset=");
            if (0 > index) {
                return null;
            }

            int end = ctype.indexOf(";", index);
            end = -1 < end ? end : ctype.length();
            charset = ctype.substring(index + 8, end);  // 8 for "charset="
            if (charset.charAt(0) == '"') {
                // The charset is enclosed in quotes, strip them
                index = charset.lastIndexOf('"');
                charset = charset.substring(1, index);
            }
        }
        return charset;
    }


    private static String getFileName(String cdl) {
        String filename = null;
        if (cdl != null) {
            cdl = cdl.trim().toLowerCase(Locale.ENGLISH);
            if (cdl.startsWith("form-data") || cdl.startsWith("attachment")) {
                int index = cdl.lastIndexOf("filename=");
                if (0 > index) {
                    return null;
                }

                int end = cdl.indexOf(";", index);
                end = -1 < end ? end : cdl.length();
                filename = cdl.substring(index + 9, end);  // 9 for "filename="
                if (filename.charAt(0) == '"') {
                    // The filename is enclosed in quotes, strip them
                    index = filename.lastIndexOf('"');
                    filename = filename.substring(1, index);
                }
            }
        }
        return filename;
    }

    private static String getName(String cdl) {
        String name = null;
        if (cdl != null) {
            cdl = cdl.trim().toLowerCase(Locale.ENGLISH);
            if (cdl.startsWith("form-data") || cdl.startsWith("attachment")) {
                int index = cdl.indexOf("name=");
                if (0 > index) {
                    return null;
                }

                int end = cdl.indexOf(";", index);
                end = -1 < end ? end : cdl.length();
                name = cdl.substring(index + 5, end);  // 5 for "name="
                if (name.charAt(0) == '"') {
                    // The name is enclosed in quotes, strip them
                    index = name.lastIndexOf('"');
                    name = name.substring(1, index);
                }
            }
        }
        return name;
    }

    private static boolean safeEquals(byte[] bytes1, int offset, int len, byte[] bytes2) {
        if (null == bytes1 && null == bytes2) {
            return true;
        }
        if (null == bytes1 || null == bytes2) {
            return false;
        }
        if (offset + len > bytes1.length) {
            throw new IndexOutOfBoundsException(offset + len + " > bytes1.length");
        }
        if (0 == offset && len == bytes2.length && bytes1 == bytes2) {
            return true;
        }
        if (len != bytes2.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (bytes1[offset + i] != bytes2[i]) {
                return false;
            }
        }
        return true;
    }

    private static final Class<?> SPRING_MULTIPART = loadClass("org.springframework.web.multipart.MultipartHttpServletRequest");

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (final Throwable ignore) {
            return null;
        }
    }

    private MultipartParser() {
    }
}
