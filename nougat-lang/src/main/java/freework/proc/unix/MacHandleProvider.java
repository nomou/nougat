package freework.proc.unix;

import com.sun.jna.Platform;
import freework.proc.Handle;
import freework.proc.spi.HandleProvider;

public class MacHandleProvider extends HandleProvider {

    @Override
    public boolean isSupported() {
        return Platform.isMac();
    }

    @Override
    public Handle current() {
        return MacHandle.current();
    }

    @Override
    public Handle of(final int pid) {
        return MacHandle.of(pid);
    }

    @Override
    public Handle of(final Process process) {
        return MacHandle.of(process);
    }

}