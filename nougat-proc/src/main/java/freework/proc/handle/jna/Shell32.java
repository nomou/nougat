package freework.proc.handle.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

/**
 * Windows shell32.
 *
 * @author vacoor
 * @since 1.0
 */
public interface Shell32 extends com.sun.jna.platform.win32.Shell32 {
    Shell32 SHELL32 = (Shell32) Native.loadLibrary("shell32", Shell32.class);

    /**
     * 解析Unicode命令行字符串.
     *
     * @param cmdline Unicode命令行字符串
     * @param ptr     命令行参数个数指针
     * @return 指向命令行参数数组的指针
     * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/shellapi/nf-shellapi-commandlinetoargvw">CommandLineToArgvW</a>
     */
    Pointer CommandLineToArgvW(final WString cmdline, final IntByReference ptr);

}