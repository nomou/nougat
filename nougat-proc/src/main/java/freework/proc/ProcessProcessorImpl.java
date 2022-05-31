package freework.proc;

import freework.io.IOUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 简单的进程器处理实现.
 *
 * @author vacoor
 */
//@Slf4j
class ProcessProcessorImpl implements ProcessProcessor {
    private String errorText = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void processInput(final OutputStream output) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processOutput(final InputStream input) throws IOException {
        IOUtils.flow(input, NullOutputStream.INSTANCE, false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processError(final InputStream input) throws IOException {
        this.errorText = IOUtils.toString(input, Charset.defaultCharset(), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finished(String[] command, int exitCode) {
        // 如果返回值不是0, 大多数都是存在错误.
        if (0 != exitCode) {
            // 如果有错误输出, 则将错误输出作为异常消息
            if (null != this.errorText && 0 < this.errorText.length()) {
//                log.warn("Program error: {} ({})", errorText, command);
                throw new IllegalStateException(errorText);
            }
//            log.warn("Program error with exit code {} ({})", exitCode, command);
            throw new IllegalStateException("exit code: " + exitCode);
        }
    }
}
