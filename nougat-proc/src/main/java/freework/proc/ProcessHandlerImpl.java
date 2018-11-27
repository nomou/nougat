package freework.proc;

import freework.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 简单的进程器处理实现
 *
 * @author vacoor
 */
class ProcessHandlerImpl implements ProcessHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessHandlerImpl.class);
    private String errorText = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void processInput(OutputStream output) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processOutput(InputStream input) throws IOException {
        IOUtils.flow(input, NullOutputStream.INSTANCE, false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processError(InputStream input) throws IOException {
        this.errorText = IOUtils.toString(input, Charset.defaultCharset(), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finished(String[] command, int exitCode) {
        // 如果返回值不是0, 大多数都是存在错误
        if (0 != exitCode) {
            // 如果有错误输出, 则将错误输出作为异常消息
            if (null != this.errorText && 0 < this.errorText.length()) {
                LOG.warn("Program error: {} ({})", errorText, command);
                throw new IllegalStateException(errorText);
            }
            LOG.warn("Program error with exit code {} ({})", exitCode, command);
            throw new IllegalStateException("exit code: " + exitCode);
        }
    }
}
