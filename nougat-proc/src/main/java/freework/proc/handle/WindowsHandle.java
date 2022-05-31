package freework.proc.handle;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import freework.proc.handle.jna.Kernel32;
import freework.proc.handle.jna.Shell32;
import org.jvnet.winp.WinProcess;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static freework.proc.handle.jna.Kernel32.KERNEL32;

class WindowsHandle extends Handle {
    private static final WindowsHandle JVM;

    static {
        JVM = new WindowsHandle(getJvmPid());
    }

    private final int pid;
    private final Process process;

    private WindowsHandle(final int pid) {
        this(pid, null);
    }

    private WindowsHandle(final int pid, final Process process) {
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
        return isAlive(pid);
    }

    @Override
    public boolean kill() {
        return killForcibly();
    }

    @Override
    public boolean killForcibly() {
        if (null != process) {
            try {
                process.destroy();
                return 0 == process.waitFor();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return kill(pid, -1);
    }

    public static WindowsHandle current() {
        return JVM;
    }

    public static WindowsHandle of(final int pid) {
        return new WindowsHandle(pid);
    }

    public static WindowsHandle of(final Process process) {
        return new WindowsHandle(getPidOnWindows(process), process);
    }

    private static int getJvmPid() {
        return KERNEL32.GetCurrentProcessId();
    }

    /**
     * https://docs.microsoft.com/en-us/windows/win32/procthread/process-security-and-access-rights.
     */
    private static final int PROCESS_QUERY_INFORMATION = 0x0400;

    private static boolean isAlive(final int pid) {
        final WinNT.HANDLE handle = KERNEL32.OpenProcess(PROCESS_QUERY_INFORMATION, false, pid);
        return null != handle;
    }

    private static boolean kill(final int pid, final int exitCode) {
        /* 根据 PID 获取进程句柄. */
        final WinNT.HANDLE handle = KERNEL32.OpenProcess(WinNT.PROCESS_TERMINATE, false, pid);
        return null != handle && KERNEL32.TerminateProcess(handle, exitCode);
    }

    private static List<String> resolveOnWindows(final int pid) {
        final String cmdline = new WinProcess(pid).getCommandLine();

        final IntByReference argc = new IntByReference();
        final Pointer argvPtr = Shell32.SHELL32.CommandLineToArgvW(new WString(cmdline), argc);
        final String[] procArgs = argvPtr.getStringArray(0, argc.getValue(), true);
        Kernel32.KERNEL32.LocalFree(argvPtr);
        return Arrays.asList(procArgs);
    }

    /**
     * Windows平台实现.
     */
    private static final String WIN32_PROCESS_CLASS = "java.lang.Win32Process";

    /**
     * Windows平台实现.
     */
    private static final String WINDOWS_PROCESS_CLASS = "java.lang.ProcessImpl";

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
}