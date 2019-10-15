package freework.proc;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinNT;
import freework.proc.jna.CLibrary;

import static freework.proc.jna.CLibrary.LIBC;
import static freework.proc.jna.Kernel32.KERNEL32;

/**
 * 进程 Killer.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class Killer {
    private static final boolean IS_WINDOWS = Platform.isWindows();

    /**
     * 安静的终止给定Windows/Unix进程.
     *
     * @param pid 进程ID
     */
    public static boolean killQuiet(final int pid) {
        return IS_WINDOWS ? killOnWindows(pid, -1) : killOnUnix(pid, CLibrary.SIGKILL);
    }

    /**
     * Kill Windows 进程.
     *
     * @param pid 进程ID
     * @return 是否成功终止进程
     */
    public static boolean killOnWindows(final int pid, final int exitCode) {
        if (!IS_WINDOWS) {
            throw new UnsupportedOperationException("this operation not support at non-windows platforms");
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
    public static boolean killOnUnix(final int pid, final int sig) {
        if (IS_WINDOWS) {
            throw new UnsupportedOperationException("this operation is not supported on the windows platform");
        }
        return 0 == LIBC.kill(pid, sig);
    }

    /**
     * Non-instantiate.
     */
    private Killer() {
    }
}
