package freework.proc.unix;

import com.sun.jna.Platform;
import freework.proc.Handle;
import freework.proc.spi.HandleProvider;

public class FreebsdHandleProvider extends HandleProvider {

    @Override
    public boolean isSupported() {
        return Platform.isFreeBSD();
    }

    @Override
    public Handle current() {
        return FreebsdHandle.current();
    }

    @Override
    public Handle of(final int pid) {
        return FreebsdHandle.of(pid);
    }

    @Override
    public Handle of(final Process process) {
        return FreebsdHandle.of(process);
    }

}