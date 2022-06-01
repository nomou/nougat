package freework.proc.handle.unix;

import com.sun.jna.Platform;
import freework.proc.handle.Handle;
import freework.proc.handle.spi.HandleProvider;

public class UnixHandleProvider extends HandleProvider {

    @Override
    public boolean isSupported() {
        return Platform.isLinux();
    }

    @Override
    public Handle current() {
        return UnixHandle.current();
    }

    @Override
    public Handle of(final int pid) {
        return UnixHandle.of(pid);
    }

    @Override
    public Handle of(final Process process) {
        return UnixHandle.of(process);
    }

}