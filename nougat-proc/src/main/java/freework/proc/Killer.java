package freework.proc;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinNT;

import static freework.proc.jna.CLibrary.LIBC;
import static freework.proc.jna.Kernel32.KERNEL32;

/**
 * 进程 Killer.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class Killer {

    /**
     * 安静的终止给定Windows/Unix进程.
     *
     * @param pid 进程ID
     */
    public static void killQuiet(final int pid) {
        if (Platform.isWindows()) {
            killOnWindows(pid, -1);
        } else {
            killOnUnix(pid, -9);
        }
    }

    /**
     * Kill Windows 进程.
     *
     * @param pid 进程ID
     * @return 是否成功终止进程
     */
    public static boolean killOnWindows(final int pid, final int exitCode) {
        if (!Platform.isWindows()) {
            throw new UnsupportedOperationException("This operation not support at non-windows platforms");
        }
        /* 根据 PID 获取进程句柄. */
        final WinNT.HANDLE handle = KERNEL32.OpenProcess(WinNT.PROCESS_TERMINATE, false, pid);
        return null != handle && KERNEL32.TerminateProcess(handle, exitCode);
    }

    /**
     * Kill Unix 进程.
     *
     * @param pid 进程ID
     * @param sig Kill 信号量
     * @return exit code
     */
    public static int killOnUnix(final int pid, final int sig) {
        if (Platform.isWindows()) {
            throw new UnsupportedOperationException("This operation is not supported on the windows platform");
        }
        return LIBC.kill(pid, sig);
    }

    private Killer() {
    }
}
