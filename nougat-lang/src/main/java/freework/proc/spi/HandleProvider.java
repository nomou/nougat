package freework.proc.spi;

import freework.proc.Handle;

public abstract class HandleProvider {

    public abstract boolean isSupported();

    public abstract Handle current();

    public abstract Handle of(final int pid);

    public abstract Handle of(final Process process);

}