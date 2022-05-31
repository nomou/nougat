package freework.proc.handle;

import freework.proc.handle.jna.CLibrary;

import java.lang.reflect.Field;

import static freework.proc.handle.jna.CLibrary.LIBC;

/**
 * UNIX 进程操作句柄.
 */
class UnixHandle extends Handle {
    private static final UnixHandle JVM = new UnixHandle(getJvmPid());

    private final int pid;
    private final Process process;

    private UnixHandle(final int pid) {
        this(pid, null);
    }

    private UnixHandle(final int pid, final Process process) {
        this.pid = pid;
        this.process = process;
    }

    @Override
    public int pid() {
        return pid;
    }

    @Override
    public boolean isAlive() {
        if (null == process) {
            return 0 == kill(pid, 0);
        }
        if (null != process) {
            // return process.isAlive();
        }
        return false;
    }

    @Override
    public boolean kill() {
        if (null != process) {
            try {
                process.destroy();
                return 0 == process.waitFor();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return 0 == kill(pid, CLibrary.SIGTERM);
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
        return 0 == kill(pid(), CLibrary.SIGKILL);
    }

    @Override
    public Info info() {
        return null;
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

    private static int getJvmPid() {
        return CLibrary.LIBC.getpid();
    }

    private static int kill(final int pid, final int signal) {
        return LIBC.kill(pid, signal);
    }

    /*
    private static void readCmdlineOnUnix(final int pid) {
        final File cmdlineFile = new File("/proc/" + pid + "/cmdline");
        try {
            final String cmdline = IOUtils.toString(new FileInputStream(cmdlineFile), StandardCharsets.UTF_8, true);
            System.out.println(cmdline);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }
    */

    private static final String UNIX_PROCESS_CLASS = "java.lang.UNIXProcess";

    private static int getUnixPid(final Process proc) {
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
}