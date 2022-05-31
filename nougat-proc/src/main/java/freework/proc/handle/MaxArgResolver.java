package freework.proc.handle;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import freework.proc.handle.jna.CLibrary;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import static com.sun.jna.Pointer.NULL;
import static freework.proc.handle.jna.CLibrary.LIBC;

public class MaxArgResolver {
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

    public static List<String> args(final int pid) {
        final IntByReference ref = new IntByReference();
        final IntByReference argMaxRef = new IntByReference(0);
        final IntByReference size = new IntByReference(SIZE_OF_INT);
        if (0 != LIBC.sysctl(new int[]{CTL_KERN, KERN_ARGMAX}, 2, argMaxRef.getPointer(), size, NULL, ref)) {
            throw new UnsupportedOperationException("Failed to get kernl.argmax: " + LIBC.strerror(Native.getLastError()));
        }

        final int argMax = argMaxRef.getValue();
        System.out.println("argmax = " + argMax);

        final ArgsPointer memory = new ArgsPointer(argMax);
        size.setValue(argMax);
        if (0 != CLibrary.LIBC.sysctl(new int[]{CTL_KERN, KERN_PROCARGS2, pid}, 3, memory, size, NULL, ref)) {
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
        final int nArgs = memory.readInt();

        // exec path
        memory.readString();
        final List<String> args = new LinkedList<>();
        for (int i = 0; i < nArgs; i++) {
            memory.skip0();
            args.add(memory.readString());
        }

        /*-
         * this is how you can read environment variables
         * while (0 != memory.peek()) {
         *    args.add(memory.readString());
         * }
         */
        return args;
    }

    private static class ArgsPointer extends Memory {
        private long offset = 0;

        ArgsPointer(final long size) {
            super(size);
        }

        int readInt() {
            final int r = getInt(offset);
            offset += SIZE_OF_INT;
            return r;
        }

        byte peek() {
            return getByte(offset);
        }

        String readString() {
            byte c;
            final ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
            while ('\0' != (c = getByte(offset++))) {
                buff.write(c);
            }
            return buff.toString();
        }

        void skip0() {
            // skip trailing '\0's
            while ('\0' == getByte(offset)) {
                offset++;
            }
        }
    }

    public static void main(String[] args) {
        final int pid = UnixHandle.current().pid();
        System.out.println(args(pid));
    }
}