package freework.proc;

import freework.proc.spi.HandleProvider;

import java.util.Arrays;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * 进程管理句柄.
 */
public abstract class Handle {

    /**
     * 进程信息.
     */
    public interface Info {

        String command();

        String[] arguments();

    }

    /**
     * 获取进程 ID.
     */
    public abstract int pid();

    /**
     * 进程是否存活.
     */
    public abstract boolean isAlive();

    /**
     * 终止进程.
     */
    public abstract boolean kill();

    /**
     * 强制终止进程.
     */
    public abstract boolean killForcibly();

    /**
     * 获取进程信息.
     */
    public abstract Info info();

    /**
     * 获取当前进程句柄.
     */
    public static Handle current() {
        return provider().current();
    }

    /**
     * 获取给定进程ID的句柄.
     *
     * @param pid 进程ID
     * @return 进程句柄
     */
    public static Handle of(final int pid) {
        return provider().of(pid);
    }

    /**
     * 获取给定 {@link Process} 句柄.
     *
     * @param process 进程实例
     * @return 进程句柄
     */
    public static Handle of(final Process process) {
        return provider().of(process);
    }

    /**
     * 简单进程信息实现.
     */
    protected static class InfoImpl implements Info {
        private final String cmdline;
        private final String command;
        private final String[] arguments;

        public InfoImpl(String cmdline, String command, String[] arguments) {
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
     * 获取当前平台的进程句柄提供者.
     */
    private static HandleProvider provider() {
        final ServiceLoader<HandleProvider> loader = ServiceLoader.load(HandleProvider.class);
        for (final HandleProvider provider : loader) {
            if (provider.isSupported()) {
                return provider;
            }
        }
        throw new ServiceConfigurationError("No available provider found for " + Handle.class.getName());
    }
}
