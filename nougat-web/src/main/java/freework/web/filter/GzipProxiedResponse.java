package freework.web.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 */
class GzipProxiedResponse extends HttpServletResponseWrapper {
    protected final HttpServletResponse delegate;
    private final int buff;
    protected ServletOutputStream stream;
    protected PrintWriter writer;

    public GzipProxiedResponse(HttpServletResponse httpResponse, int buff) {
        super(httpResponse);
        this.delegate = httpResponse;
        this.buff = buff;

        // explicitly reset content length, as the size of zipped stream is unknown
        this.delegate.setContentLength(-1);
    }

    protected ServletOutputStream doCreateOutputStream() {
        return new GzipHttpServletOutputStream(delegate, buff);
    }

    public void finish() throws IOException {
        if (null != writer) {
            writer.flush();
            writer.close();
        }
        if (null != stream) {
            stream.flush();
            stream.close();
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (null != writer) {
            throw new IllegalStateException("getWriter() has already been called for this response");
        }
        if (null == stream) {
            stream = doCreateOutputStream();
        }
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (null != writer) {
            return writer;
        }
        if (null != stream) {
            throw new IllegalStateException("getOutputStream() has already been called for this response");
        }

        String enc = delegate.getCharacterEncoding();
        stream = doCreateOutputStream();
        writer = null != enc ? new PrintWriter(new OutputStreamWriter(stream, enc)) : new PrintWriter(stream);

        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (null != stream) {
            stream.flush();
        }
    }

    /**
     * Ignores set content length on zipped stream.
     */
    @Override
    public void setContentLength(int length) {
    }

    /**
     * Servlets v3.1 introduce this method, so we need to have it here
     * in case they are used.
     */
    public void setContentLengthLong(long length) {
    }

    /* ******************************************
     *
     * ******************************************/
    static class GzipHttpServletOutputStream extends ServletOutputStream {
        private final HttpServletResponse httpResponse;
        private byte[] buf;
        private int count;
        private ServletOutputStream out;
        private GZIPOutputStream gzipOut;
        private boolean closed;

        public GzipHttpServletOutputStream(HttpServletResponse httpResponse, int bufferSize) {
            this.httpResponse = httpResponse;
            this.buf = new byte[bufferSize];
        }

        @Override
        public synchronized void write(int b) throws IOException {
            if (closed) {
                throw new IOException("Cannot write to a closed output stream");
            }
            if (count >= buf.length) {
                flushBuffer();
            }
            buf[count++] = (byte) b;
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) throws IOException {
            if (closed) {
                throw new IOException("Cannot write to a closed output stream");
            }

            if (len >= buf.length) {
                /*-
                  If the request length exceeds the size of the output buffer,
                  flush the output buffer and then write the data directly.
                  In this way buffered streams will cascade harmlessly.
                 */
                flushBuffer();
                writeToGzipStream(b, off, len);
                return;
            }

            if (len > buf.length - count) {
                flushBuffer();
            }
            System.arraycopy(b, off, buf, count, len);
            count += len;
        }

        /**
         * Flush the internal buffer
         */
        private void flushBuffer() throws IOException {
            if (0 < count) {
                writeToGzipStream(buf, 0, count);
                count = 0;
            }
        }

        protected synchronized void writeToGzipStream(byte[] buff, int off, int len) throws IOException {
            if (null == gzipOut) {
                gzipOut = new GZIPOutputStream(getInternalOutputStream());
                httpResponse.setHeader("Content-Encoding", "gzip");
            }
            gzipOut.write(buff, off, len);
        }

        private ServletOutputStream getInternalOutputStream() throws IOException {
            if (null == out) {
                out = httpResponse.getOutputStream();
            }
            return out;
        }

        @Override
        public void flush() throws IOException {
            if (closed) {
                return;
            }
            flushBuffer();
            if (null != gzipOut) {
                gzipOut.flush();
            }
        }


        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            flushBuffer();
            if (null != gzipOut) {
                gzipOut.close();
                gzipOut = null;
            }
            getInternalOutputStream().close();
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
