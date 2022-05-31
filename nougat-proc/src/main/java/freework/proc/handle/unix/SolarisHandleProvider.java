package freework.proc.handle.unix;

import com.sun.jna.Platform;
import freework.proc.handle.Handle;
import freework.proc.handle.spi.HandleProvider;

public class SolarisHandleProvider extends HandleProvider {

    @Override
    public boolean isSupported() {
        return Platform.isSolaris();
    }

    @Override
    public Handle current() {
        return SolarisHandle.current();
    }

    @Override
    public Handle of(final int pid) {
        return SolarisHandle.of(pid);
    }

    @Override
    public Handle of(final Process process) {
        return SolarisHandle.of(process);
    }

}