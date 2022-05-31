package freework.proc.handle;

import com.sun.jna.Platform;

public abstract class Handle {
    /**
     * 是否Windows平台.
     */
    private static final boolean IS_WINDOWS = Platform.isWindows();

    public abstract int pid();

    public abstract boolean isAlive();

    public abstract boolean kill();

    public abstract boolean killForcibly();

    interface Info {

        String command();

        String[] arguments();

    }

    public static Handle current() {
        return IS_WINDOWS ? WindowsHandle.current() : UnixHandle.current();
    }

    public static Handle of(final int pid) {
        return IS_WINDOWS ? WindowsHandle.of(pid) : UnixHandle.of(pid);
    }

    public static Handle of(final Process process) {
        return IS_WINDOWS ? WindowsHandle.of(process) : UnixHandle.of(process);
    }

    /*
    public static void main(String[] args) {
        final Handle handle = Handle.of(86670);
        System.out.println(handle.isAlive());
        handle.kill();
        System.out.println(handle.isAlive());
    }
    */
}
