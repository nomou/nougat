package freework.proc;

import freework.util.Arrays2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 进程启动器
 *
 * @author vacoor
 */
public class ProcessStarter {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessStarter.class);

    public interface Feedback {

        int getExitCode();

    }

    /**
     * 进程执行命令
     */
    private final String[] command;

    public ProcessStarter(String... command) {
        this.command = command;
    }

    /**
     * 在当前线程,使用给定的参数来启动一个当前命令的新进程, 并等待结束
     *
     * @param args 启动的进程命令参数
     * @return 进程的退出值, 如果被终止返回 -1
     * @throws IOException
     * @throws InterruptedException
     */
    public int execute(String... args) throws IOException, InterruptedException {
        int exitCode = -1;
        String[] cmd = merge(this.command, args);
        try {
            ProcessHandler procHandler = newHandler();
            Process process = startProcess(cmd);
            exitCode = waitForProcess(process, procHandler);
            if (null != procHandler) {
                procHandler.finished(cmd, exitCode);
            }
        } finally {
            if (LOG.isInfoEnabled()) {
                LOG.info("Program finished with exit code {} ({})", exitCode, join(" ", cmd));
            }
        }
        return exitCode;
    }

    /**
     * 在新线程, 使用给定的参数来启动一个当前命令的新进程
     *
     * @param args 启动的进程命令参数
     * @return 启动进程的 {@link ProcessTask}
     */
    public ProcessTask submit(final String... args) {
        ProcessTask task = newTask(args);
        new Thread(task).start();
        return task;
    }

    /**
     * 创建一个使用给定的参数来启动当前命令的新进程的任务
     *
     * @param args 启动的进程命令参数
     * @return 启动进程的 {@link ProcessTask}
     */
    public ProcessTask newTask(final String... args) {
        return newTask(this.command, args);
    }

    /**
     * 创建一个使用给定的命令和参数来启动新进程的任务
     *
     * @param command 进程命令
     * @param args 进程参数
     * @return 启动进程的 {@link ProcessTask}
     */
    ProcessTask newTask(final String[] command, final String... args) {
        final String[] cmd = merge(command, args);
        final Process[] process = new Process[1];

        return new ProcessTask(this, new Callable<Feedback>() {
            @Override
            public Feedback call() throws Exception {
                int code = -1;
                try {
                    ProcessHandler procHandler = newHandler();
                    process[0] = startProcess(cmd);
                    final int exitCode = code = waitForProcess(process[0], procHandler);
                    if (null != procHandler) {
                        procHandler.finished(cmd, exitCode);
                    }

                    return new Feedback() {
                        @Override
                        public int getExitCode() {
                            return exitCode;
                        }
                    };
                } finally {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Program finished with exit code {} ({})", code, join(" ", cmd));
                    }
                }
            }
        }) {
            @Override
            public String[] getCommand() {
                return command;
            }

            @Override
            public String[] getArguments() {
                return args;
            }

            @Override
            public Process getProcess() {
                return process[0];
            }
        };
    }

    /**
     * 创建一个新的进程处理器
     */
    protected ProcessHandler newHandler() {
        return new ProcessHandlerImpl();
    }

    /**
     * 使用给定的命令及参数启动一个进程
     *
     * @param command 启动进程的命令及参数
     * @throws IOException
     */
    private Process startProcess(String[] command) throws IOException {
        if (null == command || 1 > command.length) {
            throw new IllegalArgumentException("Program can't find. (empty)");
        }
        final String program = command[0];
        // TODO search

        if (LOG.isInfoEnabled()) {
            LOG.info("Execute program: " + join(" ", command));
        }

        return Runtime.getRuntime().exec(command);
    }

    /**
     * 使用给定的进程处理器等待进程结束
     *
     * @param process 要等待的进程
     * @param procHandler 进程处理器
     * @return 进程退出返回值
     * @throws InterruptedException
     * @throws IOException
     */
    private int waitForProcess(final Process process, final ProcessHandler procHandler) throws InterruptedException, IOException {
        FutureTask<Void> outputTask = null;
        FutureTask<Void> errorTask = null;

        if (null != procHandler) {
            outputTask = new FutureTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    procHandler.processOutput(process.getInputStream());
                    return null;
                }
            });
            errorTask = new FutureTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    procHandler.processError(process.getErrorStream());
                    return null;
                }
            });

            new Thread(outputTask).start();
            new Thread(errorTask).start();
        }


        try {
            if (null != outputTask) {
                outputTask.get();
            }
            if (null != errorTask) {
                errorTask.get();
            }
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();

            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException(ex);
        }

        final int returnCode = process.waitFor();
        try {
            process.getInputStream().close();
            process.getOutputStream().close();
            process.getErrorStream().close();
        } catch (final Exception ex) {
        }

        return returnCode;
    }

    protected String[] merge(String[] array1, String[] array2) {
        return Arrays2.mergeArrays(array1, array2);
    }

    private static String join(String sep, String... args) {
        if (null == args || args.length == 0) {
            return "";
        }
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (0 < i) {
                buff.append(sep);
            }
            buff.append(args[i]);
        }
        return buff.toString();
    }
}

