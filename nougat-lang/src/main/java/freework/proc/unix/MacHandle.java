package freework.proc.unix;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import freework.proc.Handle;

import java.util.Arrays;

import static com.sun.jna.Pointer.NULL;
import static freework.proc.unix.LibraryC.LIBC;

public class MacHandle extends UnixHandle {
    /**
     * Mac support.
     * <p/>
     * See http://developer.apple.com/qa/qa2001/qa1123.html
     * http://www.osxfaq.com/man/3/kvm_getprocs.ws
     * http://matburt.net/?p=16 (libkvm is removed from OSX)
     * where is kinfo_proc? http://lists.apple.com/archives/xcode-users/2008/Mar/msg00781.html
     * <p/>
     * This code uses sysctl to get the arg/env list:
     * http://www.psychofx.com/psi/trac/browser/psi/trunk/src/arch/macosx/macosx_process.c
     * which came from
     * http://www.opensource.apple.com/darwinsource/10.4.2/top-15/libtop.c
     * <p/>
     * sysctl is defined in libc.
     * <p/>
     * PS source code for Mac:
     * http://www.opensource.apple.com/darwinsource/10.4.1/adv_cmds-79.1/ps.tproj/
     */
    private static final int CTL_KERN = 1;
    private static final int KERN_ARGMAX = 8;
    private static final int KERN_PROCARGS2 = 49;
    private static final int SIZE_OF_INT = Native.getNativeSize(int.class);
    private static final MacHandle JVM = of(getJvmPid());

    private MacHandle(final int pid) {
        super(pid);
    }

    private MacHandle(final int pid, final Process process) {
        super(pid, process);
    }

    @Override
    public Handle.Info info(final int pid) {
        final IntByReference ref = new IntByReference();
        final IntByReference sysctlArgMaxRef = new IntByReference();
        final IntByReference size = new IntByReference(SIZE_OF_INT);
        if (0 != LIBC.sysctl(new int[]{CTL_KERN, KERN_ARGMAX}, 2, sysctlArgMaxRef.getPointer(), size, NULL, ref)) {
            throw new UnsupportedOperationException("Failed to get kernl.argmax: " + LIBC.strerror(Native.getLastError()));
        }

        final int sysctlArgMax = sysctlArgMaxRef.getValue();
        final Memory memory = new Memory(sysctlArgMax);
        size.setValue(sysctlArgMax);
        if (0 != LibraryC.LIBC.sysctl(new int[]{CTL_KERN, KERN_PROCARGS2, pid}, 3, memory, size, NULL, ref)) {
            throw new UnsupportedOperationException("Failed to obtain ken.procargs2: " + LIBC.strerror(Native.getLastError()));
        }

        /*
         * Make a sysctl() call to get the raw argument space of the
         * process.  The layout is documented in start.s, which is part
         * of the Csu project.  In summary, it looks like:
         *
         * /---------------\ 0x00000000
         * :               :
         * :               :
         * |---------------|
         * | argc          |
         * |---------------|
         * | arg[0]        |
         * |---------------|
         * :               :
         * :               :
         * |---------------|
         * | arg[argc - 1] |
         * |---------------|
         * | 0             |
         * |---------------|
         * | env[0]        |
         * |---------------|
         * :               :
         * :               :
         * |---------------|
         * | env[n]        |
         * |---------------|
         * | 0             |
         * |---------------| <-- Beginning of data returned by sysctl()
         * | exec_path     |     is here.
         * |:::::::::::::::|
         * |               |
         * | String area.  |
         * |               |
         * |---------------| <-- Top of stack.
         * :               :
         * :               :
         * \---------------/ 0xffffffff
         */
        final int nArgs = memory.getInt(0);

        int offset = SIZE_OF_INT;
        // skip exec path.
        while ('\0' != memory.getByte(offset)) {
            offset++;
        }

        final String[] args = new String[nArgs];
        for (int i = 0; i < nArgs; i++) {
            // skip trailing '\0's
            while ('\0' == memory.getByte(offset)) {
                offset++;
            }

            final int since = offset;
            while ('\0' != memory.getByte(offset++)) {
                ;//
            }
            args[i] = memory.getString(since);
        }

        /*
         * this is how you can read environment variables
         */
        /*
        while ('\0' != memory.getByte(++offset)) {
            final int since = offset;
            while ('\0' != memory.getByte(offset)) {
                offset++;
            }
            String string = memory.getString(since);
            System.out.println(string);
        }
        */
        return new Handle.InfoImpl(null, args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    public static MacHandle current() {
        return JVM;
    }

    public static MacHandle of(final int pid) {
        return new MacHandle(pid);
    }

    public static MacHandle of(final Process process) {
        return new MacHandle(getUnixPid(process), process);
    }

}