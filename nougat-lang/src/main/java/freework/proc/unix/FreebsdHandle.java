package freework.proc.unix;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sun.jna.Pointer.NULL;
import static freework.proc.unix.LibraryC.LIBC;

class FreebsdHandle extends UnixHandle {
    private static final int SIZE_OF_INT = Native.getNativeSize(int.class);
    private static final FreebsdHandle JVM = of(getJvmPid());

    private FreebsdHandle(final int pid) {
        super(pid);
    }

    private FreebsdHandle(final int pid, final Process process) {
        super(pid, process);
    }

    @Override
    protected Info info(final int pid) {
        /* taken from sys/sysctl.h */
        final int CTL_KERN = 1;
        final int KERN_ARGMAX = 8;
        final int KERN_PROC = 14;
        final int KERN_PROC_ARGS = 7;

        final IntByReference ref = new IntByReference();
        final IntByReference sysctlArgMaxRef = new IntByReference();
        final IntByReference size = new IntByReference();
        size.setValue(4);
        if (LIBC.sysctl(new int[]{CTL_KERN, KERN_ARGMAX}, 2, sysctlArgMaxRef.getPointer(), size, NULL, ref) != 0) {
            throw new UnsupportedOperationException("Failed to sysctl kern.argmax");
        }

        final int sysctlArgMax = sysctlArgMaxRef.getValue();
        final Memory memory = new Memory(sysctlArgMax);
        size.setValue(sysctlArgMax);
        if (LIBC.sysctl(new int[]{CTL_KERN, KERN_PROC, KERN_PROC_ARGS, pid}, 4, memory, size, NULL, ref) != 0) {
            throw new UnsupportedOperationException("Failed to obtain ken.procargs");
        }

        final List<String> args = new ArrayList<String>();
        int offset = 0;
        while (offset < size.getValue()) {
            final int since = offset;
            while ('\0' != memory.getByte(offset++)) {
            }
            args.add(memory.getString(since));
        }
        final String[] procArgs = args.toArray(new String[args.size()]);
        return new InfoImpl(null, procArgs[0], Arrays.copyOfRange(procArgs, 1, procArgs.length));
    }

    public static FreebsdHandle current() {
        return JVM;
    }

    public static FreebsdHandle of(final int pid) {
        return new FreebsdHandle(pid);
    }

    public static FreebsdHandle of(final Process process) {
        return new FreebsdHandle(getUnixPid(process), process);
    }

}