package freework.proc.unix;

import com.sun.jna.Platform;
import freework.proc.Handle;
import freework.proc.spi.HandleProvider;

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