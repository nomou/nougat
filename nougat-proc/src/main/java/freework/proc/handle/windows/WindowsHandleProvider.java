package freework.proc.handle.windows;

import com.sun.jna.Platform;
import freework.proc.handle.Handle;
import freework.proc.handle.HandleProvider;

public class WindowsHandleProvider extends HandleProvider {

    @Override
    public boolean isSupported() {
        return Platform.isWindows();
    }

    @Override
    public Handle current() {
        return WindowsHandle.current();
    }

    @Override
    public Handle of(final int pid) {
        return WindowsHandle.of(pid);
    }

    @Override
    public Handle of(final Process process) {
        return WindowsHandle.of(process);
    }

}