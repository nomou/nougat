package freework.proc;

import java.io.OutputStream;

/**
 * 类似 /dev/null 的输出流.
 */
public class NullOutputStream extends OutputStream {
    /**
     * 单例.
     */
    public static final NullOutputStream INSTANCE = new NullOutputStream();

    /**
     * Non-instantiate.
     */
    private NullOutputStream() {
    }

    /**
     * 什么也不做, 相当于输出到 /dev/null.
     *
     * @param b 要写入的字节
     */
    @Override
    public void write(final int b) {
        //to /dev/null
    }

}
