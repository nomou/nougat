package freework.proc.handle;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import org.jvnet.winp.WinProcess;
import org.jvnet.winp.WinpException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static freework.proc.handle.jna.Kernel32.KERNEL32;
import static freework.proc.handle.jna.Shell32.SHELL32;

class WindowsHandle extends Handle {
    private static final WindowsHandle JVM = new WindowsHandle(getJvmPid());

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
        return isAlive(pid);
    }

    @Override
    public boolean kill() {
        if (null == process) {
            return terminate(pid, 0);
        }
        try {
            process.destroy();
            return 0 == process.waitFor();
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean killForcibly() {
        return kill();
    }

    @Override
    public Info info() {
        return info(pid);
    }

    public static WindowsHandle current() {
        return JVM;
    }

    public static WindowsHandle of(final int pid) {
        return new WindowsHandle(pid);
    }

    public static WindowsHandle of(final Process process) {
        return new WindowsHandle(getWindowsPid(process), process);
    }

    /* ********* */

    private static int getJvmPid() {
        return KERNEL32.GetCurrentProcessId();
    }

    /**
     * https://docs.microsoft.com/en-us/windows/win32/procthread/process-security-and-access-rights.
     */
    private static final int PROCESS_TERMINATE = WinNT.PROCESS_TERMINATE;
    private static final int PROCESS_QUERY_INFORMATION = 0x0400;
    private static final int STILL_ACTIVE = 0x103;

    private static boolean isAlive(final int pid) {
        final WinNT.HANDLE handle = KERNEL32.OpenProcess(PROCESS_QUERY_INFORMATION, false, pid);
        return null != handle && STILL_ACTIVE == getExitCode(handle);
    }

    private static int getExitCode(final WinNT.HANDLE handle) {
        final IntByReference exitCodeRef = new IntByReference(0);
        if (!KERNEL32.GetExitCodeProcess(handle, exitCodeRef)) {
            throw new IllegalStateException("Failed to check status of the process with handle = " + handle.getPointer());
        }
        return exitCodeRef.getValue();
    }

    private static boolean terminate(final int pid, final int exitCode) {
        final WinNT.HANDLE handle = KERNEL32.OpenProcess(PROCESS_TERMINATE, false, pid);
        return null != handle && KERNEL32.TerminateProcess(handle, exitCode);
    }

    private static Info info(final int pid) {
        final WinProcess winProcess = new WinProcess(pid);
        try {
            final String cmdline = winProcess.getCommandLine();
            final IntByReference argc = new IntByReference();
            final Pointer argvPtr = SHELL32.CommandLineToArgvW(new WString(cmdline), argc);
            try {
                final String[] procArgs = argvPtr.getStringArray(0, argc.getValue(), true);
                return new InfoImpl(cmdline, procArgs[0], Arrays.copyOfRange(procArgs, 1, procArgs.length));
            } finally {
                KERNEL32.LocalFree(argvPtr);
            }
        } catch (final WinpException ex) {
            return null;
        }
    }

    private static class InfoImpl implements Info {
        private final String cmdline;
        private final String command;
        private final String[] arguments;

        private InfoImpl(String cmdline, String command, String[] arguments) {
            this.cmdline = cmdline;
            this.command = command;
            this.arguments = arguments;
        }

        @Override
        public String command() {
            return command;
        }

        @Override
        public String[] arguments() {
            return arguments;
        }

        @Override
        public String toString() {
            final StringBuilder buff = new StringBuilder(60);
            buff.append('[');
            if (null != command) {
                if (buff.length() > 1) {
                    buff.append(", ");
                }
                buff.append("cmd: ");
                buff.append(command);
            }
            if (null != arguments && arguments.length > 0) {
                if (buff.length() > 1) {
                    buff.append(", ");
                }
                buff.append("args: ");
                buff.append(Arrays.toString(arguments));
            }
            if (null != cmdline) {
                if (buff.length() > 1) {
                    buff.append(", ");
                }
                buff.append("cmdLine: ");
                buff.append(cmdline);
            }
            buff.append(']');
            return buff.toString();
        }
    }

    /**
     * Windows平台实现.
     */
    private static final List<String> JDK_WINDOWS_PROCESS_CLASSES = Arrays.asList("java.lang.Win32Process", "java.lang.ProcessImpl");

    /**
     * 获取 Windows 进程对象的PID.
     *
     * @param proc Windows 进程对象
     * @return 进程 PID
     */
    private static int getWindowsPid(final Process proc) {
        final Class<?> clazz = proc.getClass();
        try {
            /* 获取 windows 句柄. */
            final Field f = clazz.getDeclaredField("handle");
            f.setAccessible(true);

            /* 根据句柄获取 PID. */
            final long handle = (Long) f.get(proc);
            final WinNT.HANDLE winHandle = new WinNT.HANDLE(Pointer.createConstant(handle));
            return KERNEL32.GetProcessId(winHandle);
        } catch (final ReflectiveOperationException ex) {
            if (!JDK_WINDOWS_PROCESS_CLASSES.contains(clazz.getName())) {
                throw new IllegalStateException("process maybe not a windows process", ex);
            }
            throw new UnsupportedOperationException("windows process not supported: " + clazz, ex);
        }
    }

}