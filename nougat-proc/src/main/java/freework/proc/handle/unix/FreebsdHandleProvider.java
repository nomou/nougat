package freework.proc.handle.unix;

import com.sun.jna.Platform;
import freework.proc.handle.Handle;
import freework.proc.handle.HandleProvider;

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