package freework.proc;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This OutputStream writes all data to the famous <b>/dev/null</b>.
 * <p>
 * This output stream has no destination (file/socket etc.) and all
 * bytes written to it are ignored and lost.
 */
public class NullOutputStream extends OutputStream {

    /**
     * A singleton.
     */
    public static final NullOutputStream INSTANCE = new NullOutputStream();

    /**
     * Does mux - output to <code>/dev/null</code>.
     *
     * @param b The byte to write
     */
    @Override
    public void write(int b) {
        //to /dev/null
    }

}
