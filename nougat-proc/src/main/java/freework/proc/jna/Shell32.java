package freework.proc.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public interface Shell32 extends com.sun.jna.platform.win32.Shell32 {
    Shell32 SHELL32 = (Shell32) Native.loadLibrary("shell32", Shell32.class);

    Pointer CommandLineToArgvW(WString cmdline, IntByReference ptr);

}