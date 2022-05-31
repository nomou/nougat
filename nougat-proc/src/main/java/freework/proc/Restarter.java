package freework.proc;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;
import freework.proc.handle.Cmdline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static freework.proc.handle.jna.CLibrary.FD_CLOEXEC;
import static freework.proc.handle.jna.CLibrary.F_GETFD;
import static freework.proc.handle.jna.CLibrary.F_SETFD;
import static freework.proc.handle.jna.CLibrary.LIBC;
import static freework.proc.handle.jna.Kernel32.KERNEL32;
import static freework.proc.handle.jna.Shell32.SHELL32;

/**
 * 未完成.
 */
class Restarter {
    /**
     * 是否是Windows平台.
     */
    private static final boolean IS_WINDOWS = Platform.isWindows();

    /**
     * 是否是Mac平台.
     */
    private static final boolean IS_MAC = Platform.isMac();

    /**
     * Non-instantiate.
     */
    private Restarter() {
    }

    /**
     * 获取重启 code, 如果是非0, 则表示如果返回该 exit code来通知外部程序重启当前程序,不需要内部自行重启
     * eg:
     * boot.sh
     * LD_LIBRARY_PATH=$BIN_PATH:$LD_LIBRARY_PATH "$BIN" -Djb.restart.code=88 "$@"
     * EC=$?
     * <p/>
     * # 如果执行完毕的退出代码不是88, 则退出
     * test $EC -ne 88 && exit $EC
     * # 如果是88, 则重启
     * RESTARTER="$HOME/restarter.sh" # 执行重启前的相关命令
     * if [ -x "$RESTARTER" ]; then
     * "$RESTARTER"
     * rm -f "$RESTARTER"
     * fi
     * exec "$0" "$@"
     *
     * @return
     */
    private static int getRestartCode() {
        String code = System.getProperty("wdb.restart.code");
        if (null != code) {
            try {
                return Integer.parseInt(code);
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }
        return 0;
    }

    private static class PathManager {

        public static String getHomePath() {
            return "";
        }

    }

    private static class FileUtilRt {

        public static boolean createDirectory(File restartDir) {
            return false;
        }
    }

    private interface Consumer<T> {
        void consume(final T t);
    }

    public static boolean isSupported() {
        if (getRestartCode() != 0) {
            return true;
        }
        if (IS_WINDOWS) {
            return new File(getBinPath(), "restarter.exe").exists();
        }
        if (IS_MAC) {
            return PathManager.getHomePath().contains(".app");
        }
        return false;
    }

    public static int scheduleRestart(final String... beforeRestart) throws IOException {
        try {
            final int restartCode = getRestartCode();
            if (restartCode != 0) {
                runCommand(beforeRestart);
                return restartCode;
            } else if (IS_WINDOWS) {
                restartOnWindows(beforeRestart);
                return 0;
            } else if (IS_MAC) {
                restartOnMac(beforeRestart);
                return 0;
            }
        } catch (final Throwable t) {
            throw new IOException("cannot restart application: " + t.getMessage(), t);
        }

        runCommand(beforeRestart);
        throw new IOException("cannot restart application: not supported.");
    }

    private static void runCommand(final String... beforeRestart) throws IOException {
        if (0 == beforeRestart.length) {
            return;
        }

        final File restartDir = new File(System.getProperty("user.home") + "/." + System.getProperty("idea.paths.selector") + "/restart");
        if (!FileUtilRt.createDirectory(restartDir)) {
            throw new IOException("Cannot create dir: " + restartDir);
        }

        final File restarter = new File(restartDir, "restarter.sh");
        BufferedWriter output = new BufferedWriter(new FileWriter(restarter));
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
            output.flush();
        } finally {
            output.close();
        }

        if (!restarter.setExecutable(true, true)) {
            throw new IOException("Cannot make file executable: " + restarter);
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
        final int pid = KERNEL32.GetCurrentProcessId();
        final IntByReference argc = new IntByReference();
        final Pointer argvPointer = SHELL32.CommandLineToArgvW(KERNEL32.GetCommandLineW(), argc);
        final String[] argv = argvPointer.getStringArray(0, argc.getValue(), true);

        KERNEL32.LocalFree(argvPointer);

        doScheduleRestart(new File(getBinPath(), "restarter.exe"), new Consumer<List<String>>() {
            @Override
            public void consume(final List<String> commands) {
                Collections.addAll(commands, String.valueOf(pid), String.valueOf(beforeRestart.length));
                Collections.addAll(commands, beforeRestart);
                Collections.addAll(commands, String.valueOf(argc.getValue()));
                Collections.addAll(commands, argv);
            }
        });

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
        final String bundle = PathManager.getHomePath();
        final int p = bundle.indexOf(".app");
        if (0 > p) {
            throw new IOException("Application bundle not found: " + bundle);
        }

        final String bundlePath = bundle.substring(0, p + 4);
        doScheduleRestart(new File(getBinPath(), "restarter"), new Consumer<List<String>>() {
            @Override
            public void consume(List<String> commands) {
                Collections.addAll(commands, bundlePath);
                Collections.addAll(commands, beforeRestart);
            }
        });
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

        final String exe = ProcessUtils.getJvmExecutable();
        final List<String> args = Cmdline.resolve(ProcessUtils.getJvmPid());
        // stop current process and exec to self replace current process
        LIBC.execv(exe, new StringArray(args.toArray(new String[args.size()])));

        // 因为要在重启前执行相关命令, 因此使用停止当前进程并使用 restarter.sh 替代当前进程
        // 传递给 restarter.sh 的参数为 current_program_cmdline
        // execv(新程序名, 新程序参数), 新程序参数第一个参数为新程序自身
        File restarter = new File("");
        String program = "restarter.sh";
        BufferedWriter output = new BufferedWriter(new FileWriter(restarter));
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
            // exec program args
            output.write("exec \"$@\"");
            output.flush();
        } finally {
            output.close();
        }
        if (!restarter.setExecutable(true, true)) {
            throw new IOException("Cannot make file executable: " + restarter);
        }
        args.add(0, program);
        LIBC.execv(program, new StringArray(args.toArray(new String[args.size()])));

        throw new IOException("Failed to exec '" + exe + "' " + LIBC.strerror(Native.getLastError()));
    }

    private static void doScheduleRestart(final File restarterFile, final Consumer<List<String>> args) throws IOException {
        final List<String> commands = new ArrayList<String>();
        commands.add(createTempExecutable(restarterFile).getPath());
        args.consume(commands);
        Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]));
    }

    private static File createTempExecutable(File executable) throws IOException {
        File executableDir = new File(System.getProperty("user.home") + "/." + System.getProperty("idea.paths.selector") + "/restart");
        if (!executableDir.exists() && !executableDir.mkdirs()) {
            throw new IOException("Cannot create dir: " + executableDir);
        }
        File copy = new File(executableDir.getPath() + "/" + executable.getName());
        if (!copy.canWrite() || (copy.exists() && !copy.delete())) {
        }

        // FIXME Files.copy(executable, copy);
        if (!copy.setExecutable(executable.canExecute())) {
            throw new IOException("Cannot make file executable: " + copy);
        }
        return copy;

        /*
        if (!FileUtilRt.createDirectory(executableDir)) throw new IOException("Cannot create dir: " + executableDir);
        File copy = new File(executableDir.getPath() + "/" + executable.getName());
        if (!FileUtilRt.ensureCanCreateFile(copy) || (copy.exists() && !copy.delete())) {
            String ext = FileUtilRt.getExtension(executable.getName());
            copy = FileUtilRt.createTempFile(executableDir, FileUtilRt.getNameWithoutExtension(copy.getName()),
                    StringUtil.isEmptyOrSpaces(ext) ? ".tmp" : ("." + ext),
                    true, false);
        }
        FileUtilRt.copy(executable, copy);
        if (!copy.setExecutable(executable.canExecute())) throw new IOException("Cannot make file executable: " + copy);
        return copy;
        */
    }

    private static File getBinPath() {
        return new File("D:\\Applications\\JetBrains\\IntelliJ IDEA 14.1.7\\bin");
    }

    public static void main(String[] args) {

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