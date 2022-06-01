package freework.proc.handle.unix;

import freework.io.IOUtils;
import freework.proc.handle.Handle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

import static freework.proc.handle.unix.LibraryC.LIBC;

/**
 * UNIX 进程操作句柄.
 */
class UnixHandle extends Handle {
    private static final UnixHandle JVM = of(getJvmPid());
    private final int pid;
    private final Process process;

    UnixHandle(final int pid) {
        this(pid, null);
    }

    UnixHandle(final int pid, final Process process) {
        this.pid = pid;
        this.process = process;
    }

    @Override
    public int pid() {
        return pid;
    }

    @Override
    public boolean isAlive() {
        return 0 == kill(pid, 0);
    }

    @Override
    public boolean kill() {
        return 0 == kill(pid, LibraryC.SIGTERM);
    }

    @Override
    public boolean killForcibly() {
        if (null != process) {
            try {
                // process.destroyForcibly();
                process.destroy();
                return 0 == process.waitFor();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return 0 == kill(pid(), LibraryC.SIGKILL);
    }

    @Override
    public Info info() {
        return isAlive() ? info(pid) : null;
    }

    protected Info info(final int pid) {
        final File cmdline = new File("/proc/" + pid + "/cmdline");
        if (!cmdline.exists()) {
            throw new UnsupportedOperationException("/proc/" + pid + "/cmdline not found");
        }
        try {
            final String cmdlineStr = IOUtils.toString(new FileReader(cmdline), true);
            final String[] segments = cmdlineStr.split("\0");
            return new InfoImpl(cmdlineStr, segments[0], Arrays.copyOfRange(segments, 1, segments.length));
        } catch (final IOException e) {
            throw new IllegalStateException("Can not read cmdline: " + cmdline);
        }
    }

    public static UnixHandle current() {
        return JVM;
    }

    public static UnixHandle of(final int pid) {
        return new UnixHandle(pid);
    }

    public static UnixHandle of(final Process process) {
        return new UnixHandle(getUnixPid(process), process);
    }

    /* ********** */

    static int getJvmPid() {
        return LibraryC.LIBC.getpid();
    }

    static int kill(final int pid, final int signal) {
        return LIBC.kill(pid, signal);
    }

    private static final String UNIX_PROCESS_CLASS = "java.lang.UNIXProcess";

    static int getUnixPid(final Process proc) {
        final Class<? extends Process> clazz = proc.getClass();
        try {
            final Field pidField = clazz.getDeclaredField("pid");
            pidField.setAccessible(true);
            return (Integer) pidField.get(proc);
        } catch (final ReflectiveOperationException ex) {
            if (!UNIX_PROCESS_CLASS.equals(clazz.getName())) {
                throw new IllegalStateException("process maybe not a unix process", ex);
            }
            throw new UnsupportedOperationException("unix process not supported: " + clazz, ex);
        }
    }


    /* ****** */
}