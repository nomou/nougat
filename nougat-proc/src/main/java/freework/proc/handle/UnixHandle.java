package freework.proc.handle;

import freework.io.IOUtils;
import freework.proc.handle.jna.CLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static freework.proc.handle.jna.CLibrary.LIBC;

/**
 * UNIX 进程操作句柄.
 */
class UnixHandle extends Handle {
    private static final UnixHandle JVM;

    static {
        JVM = new UnixHandle(getJvmPid());
    }

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
        if (null != process) {
            return process.isAlive();
        }
        return 0 == kill(pid, 0);
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
                process.destroyForcibly();
                return 0 == process.waitFor();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return 0 == kill(pid(), CLibrary.SIGKILL);
    }

    public static UnixHandle current() {
        return JVM;
    }

    public static UnixHandle of(final int pid) {
        return new UnixHandle(pid);
    }

    public static UnixHandle of(final Process process) {
        return new UnixHandle(getPidOnUnix(process), process);
    }

    private static int getJvmPid() {
        return CLibrary.LIBC.getpid();
    }

    private static int kill(final int pid, final int signum) {
        return LIBC.kill(pid, signum);
    }

    private static void readCmdlineOnUnix(final int pid) {
        final File cmdlineFile = new File("/proc/" + pid + "/cmdline");
        try {
            final String cmdline = IOUtils.toString(new FileInputStream(cmdlineFile), StandardCharsets.UTF_8, true);
            System.out.println(cmdline);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String UNIX_PROCESS_CLASS = "java.lang.UNIXProcess";

    private static int getPidOnUnix(final Process proc) {
        final Class<? extends Process> clazz = proc.getClass();
        final String type = clazz.getName();
        if (!UNIX_PROCESS_CLASS.equals(type)) {
            throw new IllegalStateException("unsupported unix process: " + type);
        }

        try {
            final Field pidField = clazz.getDeclaredField("pid");
            if (!pidField.isAccessible()) {
                pidField.setAccessible(true);
            }
            return (Integer) pidField.get(proc);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) throws IOException {
//        readCmdlineOnUnix(current().pid);
//        Cmdline resolve = Cmdline.resolve(current().pid);
        List<String> _args = MaxArgResolver.args(current().pid);
        System.out.println(_args);
        ProcessHandle.Info info = ProcessHandle.current().info();
        String s = info.command().get();
        info.commandLine();
        System.out.println(s);
    }
}