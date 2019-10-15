package freework.proc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 进程启动器.
 *
 * @author vacoor
 */
public class ProcessStarter {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessStarter.class);

    /**
     * 进程执行命令.
     */
    private final String[] command;

    /**
     * 创建一个 ProcessStarter.
     *
     * @param command 启动命令
     */
    public ProcessStarter(final String... command) {
        this.command = command;
    }

    /**
     * 在当前线程, 使用给定的参数来为当前命令启动新进程, 并等待结束.
     *
     * @param args 启动的进程命令参数
     * @return 进程的退出值, 如果被终止返回 -1
     * @throws IOException          发生IO错误抛出此异常
     * @throws InterruptedException 如果被进程打断抛出此异常
     */
    public int execute(final String... args) throws IOException, InterruptedException {
        int exitCode = -1;
        final String[] cmd = merge(this.command, args);
        try {
            final ProcessProcessor processor = newProcessor();
            final Process process = startProcess(cmd);

            exitCode = waitForProcess(process, processor);
            if (null != processor) {
                processor.finished(cmd, exitCode);
            }
        } finally {
            if (LOG.isInfoEnabled()) {
                LOG.info("Program finished with exit code {} ({})", exitCode, join(" ", cmd));
            }
        }
        return exitCode;
    }

    /**
     * 创建一个使用给定的参数来启动当前命令的新进程的任务.
     *
     * @param args 启动的进程命令参数
     * @return 启动进程的 {@link ProcessTask}
     */
    public ProcessTask newTask(final String... args) {
        return newTask(this.command, args);
    }

    /**
     * 创建一个使用给定的命令和参数来启动新进程的任务.
     *
     * @param command 进程命令
     * @param args    进程参数
     * @return 启动进程的 {@link ProcessTask}
     */
    ProcessTask newTask(final String[] command, final String... args) {
        final String[] cmd = merge(command, args);
        final Process[] process = new Process[1];

        return new ProcessTask(this, new Callable<Integer>() {
            /**
             * {@inheritDoc}
             */
            @Override
            public Integer call() throws Exception {
                int code = -1;
                try {
                    final ProcessProcessor processor = newProcessor();
                    process[0] = startProcess(cmd);

                    final int exitCode = code = waitForProcess(process[0], processor);
                    if (null != processor) {
                        processor.finished(cmd, exitCode);
                    }
                    return exitCode;
                } finally {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Program finished with exit code {} ({})", code, join(" ", cmd));
                    }
                }
            }
        }) {
            /**
             * {@inheritDoc}
             */
            @Override
            public String[] getCommand() {
                return command;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String[] getArguments() {
                return args;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Process getProcess() {
                return process[0];
            }
        };
    }

    /**
     * 创建新的进程处理器.
     */
    protected ProcessProcessor newProcessor() {
        return new ProcessProcessorImpl();
    }

    /**
     * 启动一个进程.
     *
     * @param command 启动进程的命令及参数
     * @throws IOException 如果IO错误抛出此异常
     */
    private Process startProcess(final String[] command) throws IOException {
        if (null == command || 1 > command.length) {
            throw new IllegalArgumentException("Program can't find. (empty)");
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Execute program: {}", join(" ", command));
        }
        return Runtime.getRuntime().exec(command);
    }

    /**
     * 等待进程结束.
     *
     * @param process   要等待的进程
     * @param processor 进程处理器
     * @return 进程退出返回值
     * @throws InterruptedException 如果进程被打断
     * @throws IOException          如果发生IO错误
     */
    private int waitForProcess(final Process process, final ProcessProcessor processor) throws InterruptedException, IOException {
        FutureTask<Void> outputTask = null;
        FutureTask<Void> errorTask = null;

        if (null != processor) {
            outputTask = new FutureTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    processor.processOutput(process.getInputStream());
                    return null;
                }
            });
            errorTask = new FutureTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    processor.processError(process.getErrorStream());
                    return null;
                }
            });

            this.doRunTask(outputTask, errorTask);
        }

        try {
            if (null != outputTask) {
                outputTask.get();
            }
            if (null != errorTask) {
                errorTask.get();
            }
        } catch (final ExecutionException ex) {
            final Throwable cause = ex.getCause();
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
            // ignore
        }

        return returnCode;
    }

    protected void doRunTask(final Runnable... tasks) {
        for (final Runnable task : tasks) {
            new Thread(task).start();
        }
    }

    private String[] merge(final String[] array1, final String[] array2) {
        final String[] merged = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, merged, array1.length, array2.length);
        return merged;
    }

    private static String join(final String sep, final String... args) {
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

