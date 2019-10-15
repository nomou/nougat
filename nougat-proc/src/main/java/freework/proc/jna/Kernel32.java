package freework.proc.jna;

import com.sun.jna.Native;
import com.sun.jna.WString;

/**
 * Windows kernel32.
 *
 * @author vacoor
 */
public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {
    Kernel32 KERNEL32 = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);

    /**
     * 获取当前进程的命令行参数.
     *
     * @return 当前进程的命令行参数
     * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/processenv/nf-processenv-getcommandlinew">GetCommandLineW</a>
     */
    WString GetCommandLineW();

    /**
     * 获取当前进程的进程ID.
     *
     * @return 当前进程ID
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms683180(VS.85).aspx">GetCurrentProcessId</a>
     */
    @Override
    int GetCurrentProcessId();

    /**
     * 打开一个已经存在的进程对象.
     *
     * @return 进程句柄
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms684320(VS.85).aspx">OpenProcess</a>
     */
    @Override
    HANDLE OpenProcess(final int dwDesiredAccess, final boolean bInheritHandle, final int dwProcessId);

}