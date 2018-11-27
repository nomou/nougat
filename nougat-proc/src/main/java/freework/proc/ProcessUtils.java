package freework.proc;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import freework.proc.jna.CLibrary;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;

import static freework.proc.jna.Kernel32.KERNEL32;

/**
 * 进程工具类.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class ProcessUtils {
    private static final String WIN32_PROCESS_CLASS = "java.lang.Win32Process";
    private static final String WINDOWS_PROCESS_CLASS = "java.lang.ProcessImpl";
    private static final String UNIX_PROCESS_CLASS = "java.lang.UNIXProcess";

    /**
     * 获取给定进程的进程ID.
     *
     * @param proc 进程
     * @return 进程ID
     */
    public static int getPid(final Process proc) {
        return Platform.isWindows() ? getPidOnWindows(proc) : getPidOnWindows(proc);
    }

    public static Cmdline getCmdline(final int pid) throws IOException {
        return Cmdline.resolve(pid);
    }

    public static Cmdline getCmdline(final Process proc) throws IOException {
        return getCmdline(getPid(proc));
    }

    /**
     * 获取当前JVM PID.
     *
     * @return 当前JVM进程ID
     */
    public static int getJvmPid() {
        int jvmPid = -1;
        try {
            if (Platform.isWindows()) {
                jvmPid = KERNEL32.GetCurrentProcessId();
            } else {
                jvmPid = CLibrary.LIBC.getpid();
            }
        } catch (final Exception ignore) {
            // ignore
        }

        if (0 > jvmPid) {
            final String rmbName = ManagementFactory.getRuntimeMXBean().getName();
            final String pidStr = (-1 < rmbName.indexOf('@')) ? rmbName.substring(0, rmbName.indexOf('@')) : null;
            if (null != pidStr) {
                try {
                    jvmPid = Integer.parseInt(pidStr);
                } catch (final NumberFormatException ignore) {
                    // ignore
                }
            }
        }

        if (0 > jvmPid) {
            throw new IllegalStateException("cannot get jvm pid on the platform");
        }
        return jvmPid;
    }

    /**
     * 获取当前 jvm 可执行文件路径.
     *
     * @return java 可执行文件路径
     */
    public static String getJvmExecutable() {
        if (!Platform.isWindows()) {
            final int pid = CLibrary.LIBC.getpid();
            final String name = "/proc/" + pid + "/exe";
            if (new File(name).exists()) {
                return name;
            }
        }

        // cross-platform fallback
        final String bin = System.getProperty("java.home") + "/bin/java";
        if (new File(bin).exists()) {
            return bin;
        }

        final String exe = bin + ".exe";
        final File executable = new File(exe);
        if (executable.exists() && executable.canExecute()) {
            return exe;
        }
        throw new IllegalStateException("cannot found jvm executable path");
    }

    /**
     * 获取 Windows 进程对象的PID.
     *
     * @param proc Windows 进程对象
     * @return 进程 PID
     */
    private static int getPidOnWindows(final Process proc) {
        final Class<?> clazz = proc.getClass();
        final String type = clazz.getName();

        if (!WIN32_PROCESS_CLASS.equals(type) && !WINDOWS_PROCESS_CLASS.equals(type)) {
            throw new IllegalStateException("process is not a windows process");
        }

        try {
            /* 获取 windows 句柄. */
            final Field handleField = clazz.getDeclaredField("handle");
            if (!handleField.isAccessible()) {
                handleField.setAccessible(true);
            }

            /* 根据句柄获取 PID. */
            final long handle = (Long) handleField.get(proc);
            final WinNT.HANDLE winHandle = new WinNT.HANDLE();
            winHandle.setPointer(Pointer.createConstant(handle));

            return KERNEL32.GetProcessId(winHandle);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 获取 Unix 进程对象的PID.
     *
     * @param proc Unix进程对象
     * @return 进程PID
     */
    private static int getPidOnUnix(final Process proc) {
        final Class<? extends Process> clazz = proc.getClass();
        final String type = clazz.getName();

        if (!UNIX_PROCESS_CLASS.equals(type)) {
            throw new IllegalStateException("process is not a unix process");
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

    public static void main(String[] args) {
        int jvmPid = ProcessUtils.getJvmPid();
        System.out.println(jvmPid);
    }
}
