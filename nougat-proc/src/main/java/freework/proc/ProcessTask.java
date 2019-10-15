package freework.proc;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 进程任务.
 *
 * @author vacoor
 */
public abstract class ProcessTask extends FutureTask<Integer> {
    private final ProcessStarter starter;

    protected ProcessTask(final ProcessStarter starter, Callable<Integer> callable) {
        super(callable);
        this.starter = starter;
    }

    /**
     * 获取创建该进程任务的 {@link ProcessStarter}
     */
    public ProcessStarter getStarter() {
        return starter;
    }

    /**
     * 取消当前进程任务
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        final Process process = getProcess();
        if (null != process && !isDone()) {
            process.destroy();
        }
        return super.cancel(mayInterruptIfRunning);
    }

    /**
     * 拷贝当前进程任务作为一个新任务.
     */
    public ProcessTask copyAsNew() {
        return starter.newTask(getCommand(), getArguments());
    }

    /**
     * 获取当前进程任务创建时指定的命令.
     */
    public abstract String[] getCommand();

    /**
     * 获取当前进程任务创建时的参数
     */
    public abstract String[] getArguments();

    /**
     * 获取当前进程任务中正在执行的进程.
     *
     * @return 如果任务已经执行则返回正在执行的进程，否则返回 null
     */
    public abstract Process getProcess();

}
