package freework.proc.handle;

import java.util.Arrays;

public abstract class Handle {

    public interface Info {

        String command();

        String[] arguments();

    }

    public abstract int pid();

    public abstract boolean isAlive();

    public abstract boolean kill();

    public abstract boolean killForcibly();

    public abstract Info info();

    public static Handle current() {
        return HandleProvider.provider().current();
    }

    public static Handle of(final int pid) {
        return HandleProvider.provider().of(pid);
    }

    public static Handle of(final Process process) {
        return HandleProvider.provider().of(process);
    }

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
}
