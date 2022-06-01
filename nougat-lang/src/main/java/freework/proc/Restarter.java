package freework.proc;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;
import freework.io.IOUtils;
import freework.proc.windows.Kernel32;
import freework.proc.windows.Shell32;
import freework.util.Unpacker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static freework.proc.unix.LibraryC.*;

/**
 * 未完成.
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
            /* } else if (Platform.isMac()) {
                restartOnMac(beforeRestart); */
            } else if (Platform.isLinux()) {
                restartOnUnix(beforeRestart);
            } else {
                throw new IOException("cannot restart application: not supported.");
            }
        } catch (final Throwable t) {
            throw new IOException("cannot restart application: " + t.getMessage(), t);
        }
    }

    /**
     * 执行Windows平台的重启操作.
     *
     * @param beforeRestart 重启前要执行的命令行
     * @throws IOException 如果重启指令出错抛出该异常
     */
    private static void restartOnWindows(final String... beforeRestart) throws IOException {
        /* 获取当前进程启动命令行. */
        final int pid = Kernel32.KERNEL32.GetCurrentProcessId();
        final IntByReference argc = new IntByReference();
        final Pointer argvPointer = Shell32.SHELL32.CommandLineToArgvW(Kernel32.KERNEL32.GetCommandLineW(), argc);
        try {
            final String[] argv = argvPointer.getWideStringArray(0, argc.getValue());
            doScheduleRestart(getRestarterBin(), new Consumer<List<String>>() {
                @Override
                public void accept(final List<String> commands) {
                    Collections.addAll(commands, String.valueOf(pid));

                    if (0 < beforeRestart.length) {
                        Collections.addAll(commands, String.valueOf(beforeRestart.length));
                        Collections.addAll(commands, beforeRestart);
                    }

                    Collections.addAll(commands, String.valueOf(argc.getValue()));
                    Collections.addAll(commands, argv);
                }
            });
        } finally {
            Kernel32.KERNEL32.LocalFree(argvPointer);
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

    /**
     * 执行MAC平台的Bundle重启操作.
     *
     * @param beforeRestart 重启前要执行的命令行
     * @throws IOException 如果重启指令出错抛出该异常
     */
    private static void restartOnMac(final String... beforeRestart) throws IOException {
        // FIXME
//        final String bundle = "/Applications/Developer Tools/IntelliJ IDEA.app";
        final String bundle = "/Applications/QQ.app";
        final int p = bundle.indexOf(".app");
        if (0 > p) {
            throw new IOException("Application bundle not found: " + bundle);
        }

        final String bundlePath = bundle.substring(0, p + 4);
        /*-
         * mac restarter, getppid != 1, usleep 0.5 second, until ppid destroy
         */
        doScheduleRestart(getRestarterBin(), new Consumer<List<String>>() {
            @Override
            public void accept(List<String> commands) {
                Collections.addAll(commands, bundlePath);
                Collections.addAll(commands, beforeRestart);
            }
        });
    }

    private static void doScheduleRestart(final File restarterBin, final Consumer<List<String>> args) throws IOException {
        final List<String> commands = new ArrayList<String>();
        commands.add(restarterBin.getAbsolutePath());
        args.accept(commands);
        System.out.println(commands);
        Process exec = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]));
//        Threads.sleep(2000);
//        System.out.println(Handle.of(exec).isAlive());
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
        System.out.println(restarter);

        final String[] cmdlineToUse = new String[cmdline.length + 1];
        cmdlineToUse[0] = restarter.getAbsolutePath();
        System.arraycopy(cmdline, 0, cmdlineToUse, 1, cmdline.length);

        System.out.println(String.format("LIBC.execv(%s, %s)", cmdlineToUse[0], Arrays.toString(cmdlineToUse)));
        LIBC.execv(cmdlineToUse[0], new StringArray(cmdlineToUse));

        throw new IOException("Failed to exec '" + exec + "' " + LIBC.strerror(Native.getLastError()));
    }

    private interface Consumer<T> {
        void accept(final T t);
    }

    private static final File RESTARTER;

    static {
        /*-
         * restarter pid [argc arg1 ...] [argc arg1, ...]] ...
         * eg:
         * restarter {pid} 1 c:\windows\notepad.exe 2 c:\windows\notepad.exe foo.txt
         * wait pid exit -> notepad.exe
         * wait notepad.exe exit -> notepad.exe foo.txt
         * ...
         */
        final boolean is64Bit = Platform.is64Bit();
        if (Platform.isWindows()) {
            RESTARTER = makeExecutable(Unpacker.unpackAsTempFile(Restarter.class.getResource("/bin/windows/x86/restarter.exe")));
        } else if (Platform.isMac() && is64Bit) {
            RESTARTER = makeExecutable(Unpacker.unpackAsTempFile(Restarter.class.getResource("/bin/macosx/x86_64/restarter")));
        } else {
            RESTARTER = null;
        }
    }

    private static File makeExecutable(final File f) {
        if (!f.setExecutable(true)) {
            throw new IllegalStateException("Cannot make file executable: " + f);
        }
        return f;
    }

    private static File getRestarterBin() {
        return RESTARTER;
    }

    public static void main(String[] args) throws Exception {
//         args = new String[]{"log.log", "2022-06-01 21:06:00"};
        /*
        System.out.println("Wait restarter");
        while (true) {
            final Process proc = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "ps -ef | grep restarter | grep -v grep | awk '{print $2}'"});
            final InputStream in = proc.getInputStream();
            final String pid = IOUtils.toString(in, Charset.defaultCharset(), true);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (null != pid && !pid.isEmpty()) {
                System.out.println(Handle.of(Integer.parseInt(pid.trim())).info());
                System.out.println(pid);
                break;
            }
            TimeUnit.SECONDS.sleep(1);
        }
        */

        /*
        final Process proc = Runtime.getRuntime().exec(new String[]{
                "/Applications/Developer Tools/IntelliJ IDEA.app/Contents/bin/restarter",
                "/Applications/Developer Tools/IntelliJ IDEA.app"
        });
        Thread.sleep(600);
        System.out.println("Exit");
        System.exit(88);
        */

        System.out.println(System.getProperty("os.name"));
        Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(args[1]);
        if (parse.getTime() > System.currentTimeMillis()) {
            final FileWriter writer = new FileWriter(args[0], true);
            writer.write(System.currentTimeMillis() + ": OK\r\n");
            IOUtils.close(writer);
//            Threads.sleep(1000);
            restartOnMac();
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
        /*
        Excels.read(IOUtils.buffer(Resources.getResourceAsStream("ds_product-97-2003.xls")), new RecordHandlerImpl() {
            @Override
            public void onRecord(Record record) throws ExcelReadException {
                ExcelHelper.printRecord(record);
//                System.out.println();
            }
        });
        */
    }
}