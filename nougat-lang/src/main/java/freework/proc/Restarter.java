package freework.proc;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;
import freework.io.IOUtils;
import freework.thread.Threads;
import freework.util.LazyValue;
import freework.util.Unpacker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static freework.proc.unix.LibraryC.*;
import static freework.proc.windows.Kernel32.KERNEL32;
import static freework.proc.windows.Shell32.SHELL32;

/**
 *
 */
public class Restarter {
    /**
     * Non-instantiate.
     */
    private Restarter() {
    }

    public static void scheduleRestart(final String... beforeRestart) throws IOException {
        try {
            if (Platform.isWindows()) {
                restartOnWindows(beforeRestart);
            } else if (Platform.isMac()) {
                restartOnMac(beforeRestart);
            } else if (Platform.isLinux()) {
                restartOnUnix(beforeRestart);
            } else {
                throw new IOException("cannot restart application: not supported.");
            }
        } catch (final Throwable t) {
            throw new IOException("cannot restart application: " + t.getMessage(), t);
        }
    }

    private static void restartOnWindows(final String... beforeRestart) throws IOException {
        final int pid = KERNEL32.GetCurrentProcessId();
        final IntByReference argc = new IntByReference();
        final Pointer argvPointer = SHELL32.CommandLineToArgvW(KERNEL32.GetCommandLineW(), argc);
        try {
            final String[] argv = argvPointer.getWideStringArray(0, argc.getValue());
            /*-
             * See https://blogs.msdn.microsoft.com/oldnewthing/20060515-07/?p=31203
             * argv[0] as the program name is only a convention, i.e. there is no guarantee the name is the full path to the executable
             */
            /*
            // using 32,767 as buffer size to avoid limiting ourselves to MAX_PATH (260)
            final char[] buffer = new char[32767];
            final int result = KERNEL32.GetModuleFileNameW(null, buffer, new WinDef.DWORD(buffer.length)).intValue();
            if (result == 0) {
            throw new IOException("GetModuleFileName failed")
            }
            argv[0] = Native.toString(buffer);
            */
            final List<String> args = new ArrayList<>();
            Collections.addAll(args, String.valueOf(pid));
            if (0 < beforeRestart.length) {
                Collections.addAll(args, String.valueOf(beforeRestart.length));
                Collections.addAll(args, beforeRestart);
            }
            Collections.addAll(args, String.valueOf(argc.getValue()));
            Collections.addAll(args, argv);

            final File restarter = RESTARTER.get();
            if (null == restarter) {
                throw new IOException("Can't find restarter.exe, please check jar integrity");
            }
            runRestarter(restarter, args);
        } finally {
            KERNEL32.LocalFree(argvPointer);
        }

        /*-
         * Since the process ID is passed through the command line, we want to make sure that we don't exit before the "restarter"
         * process has a chance to open the handle to our process, and that it doesn't wait for the termination of an unrelated
         * process which happened to have the same process ID.
         */
        try {
            Thread.sleep(500);
        } catch (final InterruptedException ignore) {
            /* ignore. */
        }
    }

    private static void restartOnMac(final String... beforeRestart) throws IOException {
        final Handle handle = Handle.current();
        final String command = handle.info().command();
        final int p = command.indexOf(".app");
        if (0 > p) {
            restartOnUnix(beforeRestart);
        } else {
            restartBundleOnMac(command.substring(0, p + 4));
        }
    }

    private static void restartBundleOnMac(final String bundle, final String... beforeRestart) throws IOException {
        final int p = bundle.indexOf(".app");
        if (0 > p) {
            throw new IOException("Application bundle not found: " + bundle);
        }

        /*-
         * mac restarter, getppid != 1, usleep 0.5 second, until ppid destroy
         */
        final List<String> args = new ArrayList<>();
        Collections.addAll(args, bundle.substring(0, p + 4));
        Collections.addAll(args, beforeRestart);

        final File restarter = RESTARTER.get();
        if (null == restarter) {
            throw new IOException("Can't find restarter, please check jar integrity");
        }
        runRestarter(restarter, args);
    }

    private static void restartOnUnix(final String... beforeRestart) throws IOException {
        // close all files upon exec, except stdin, stdout, and stderr
        final int size = LIBC.getdtablesize();
        for (int i = 3; i < size; i++) {
            final int flags = LIBC.fcntl(i, F_GETFD);
            if (0 > flags) {
                continue;
            }
            LIBC.fcntl(i, F_SETFD, flags | FD_CLOEXEC);
        }

        final Handle.Info info = Handle.current().info();
        final String exec = info.command();
        final String[] args = info.arguments();
        final String[] cmdline = new String[args.length + 1];
        cmdline[0] = exec;
        System.arraycopy(args, 0, cmdline, 1, args.length);

        /*-
         * If you don't need to execute HOOK.
         * stop current process and exec to self replace current process
         * LIBC.execv(cmdline[0], new StringArray(cmdline));
         */

        // 因为要在重启前执行相关命令, 因此使用停止当前进程并使用 restarter.sh 替代当前进程
        // 传递给 restarter.sh 的参数为 current_program_cmdline
        // execv(新程序名, 新程序参数), 新程序参数第一个参数为新程序自身
        final File restarter = File.createTempFile("restarter", ".sh");
        final BufferedWriter output = new BufferedWriter(new FileWriter(restarter));
        try {
            output.write("#!/bin/sh\n");
            for (int i = 0; i < beforeRestart.length; i++) {
                output.write(beforeRestart[i]);
                if (i <= beforeRestart.length - 2) {
                    output.write(' ');
                }
                if (i >= beforeRestart.length - 2) {
                    output.write('"');
                }
            }
            output.write('\n');
            output.write("rm \"$0\"\n");
            // exec program args
            output.write("exec \"$@\"");
            output.flush();
        } finally {
            output.close();
        }

        if (!restarter.setExecutable(true)) {
            throw new IOException("Cannot make file executable: " + restarter);
        }

        final String[] cmdlineToUse = new String[cmdline.length + 1];
        cmdlineToUse[0] = restarter.getAbsolutePath();
        System.arraycopy(cmdline, 0, cmdlineToUse, 1, cmdline.length);

        LIBC.execv(cmdlineToUse[0], new StringArray(cmdlineToUse));

        throw new IOException("Failed to exec '" + exec + "' " + LIBC.strerror(Native.getLastError()));
    }

    private static final LazyValue<File> RESTARTER = new LazyValue<File>() {
        @Override
        protected File compute() {
            /*-
             * https://github.com/JetBrains/intellij-community/tree/master/native
             */
            final boolean is64Bit = Platform.is64Bit();
            URL bin = null;
            if (Platform.isWindows()) {
                bin = Restarter.class.getResource("/bin/windows/x86/restarter.exe");
            } else if (Platform.isMac() && is64Bit) {
                bin = Restarter.class.getResource("/bin/macosx/x86_64/restarter");
            }
            return null != bin ? makeExecutable(Unpacker.unpackToDirectory(bin, new File("bin"), false)) : null;
        }
    };

    private static File makeExecutable(final File f) {
        if (!f.setExecutable(true)) {
            throw new IllegalStateException("Cannot make file executable: " + f);
        }
        return f;
    }

    private static void runRestarter(final File restarterFile, final List<String> restarterArgs) throws IOException {
        restarterArgs.add(0, restarterFile.getAbsolutePath());
        new ProcessBuilder(restarterArgs).start();
    }

    public static void main(String[] args) throws Exception {
        // args = new String[]{"log.log", "2022-06-02 15:33:00"};

        System.out.println(System.getProperty("os.name"));
        Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(args[1]);
        if (parse.getTime() > System.currentTimeMillis()) {
            final FileWriter writer = new FileWriter(args[0], true);
            writer.write(System.currentTimeMillis() + ": OK\r\n");
            IOUtils.close(writer);
            Threads.sleep(1000);
            scheduleRestart();
        }
        /*
        String[] beforeRestart = {"ab", "cd", "cd", "sdinf", "sdf"};
        System.out.print("#!/bin/sh\n");
        for (int i = 0; i < beforeRestart.length; i++) {
            System.out.print(beforeRestart[i]);
            if (i <= beforeRestart.length - 2) System.out.print(' ');
            if (i >= beforeRestart.length - 2) System.out.print('"');
        }
        System.out.print('\n');
        */
    }
}