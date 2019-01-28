package freework.proc;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import freework.proc.jna.CLibrary;
import freework.proc.jna.Kernel32;
import freework.proc.jna.Shell32;
import org.jvnet.winp.WinProcess;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static com.sun.jna.Pointer.NULL;
import static java.util.logging.Level.FINEST;

//import com.sun.akuma.Daemon;

/**
 * List of arguments for Java VM and application.
 *
 * @author Kohsuke Kawaguchi
 */
public class Cmdline extends ArrayList<String> {
    private static final boolean IS_LITTLE_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"));
    private static final Logger LOGGER = Logger.getLogger(Cmdline.class.getName());

    public Cmdline() {
    }

    public Cmdline(final Collection<? extends String> c) {
        super(c);
    }


    public void removeSystemProperty(String name) {
        name = "-D" + name;
        String nameeq = name + '=';
        for (Iterator<String> itr = this.iterator(); itr.hasNext(); ) {
            String s = itr.next();
            if (s.equals(name) || s.startsWith(nameeq)) {
                itr.remove();
            }
        }
    }

    public void setSystemProperty(String name, String value) {
        removeSystemProperty(name);
        // index 0 is the executable name
        add(1, "-D" + name + "=" + value);
    }

    /**
     * Removes the n items from the end.
     * Useful for removing all the Java arguments to rebuild them.
     */
    public void removeTail(int n) {
        removeAll(subList(size() - n, size()));
    }

    /**
     * Gets the process argument list of the specified process ID.
     *
     * @param pid -1 to indicate the current process.
     */
    public static Cmdline resolve(int pid) throws IOException {
        String os = System.getProperty("os.name");
        if ("Linux".equals(os)) {
            return resolveOnLinux(pid);
        }
        if ("SunOS".equals(os)) {
            return resolveOnSolaris(pid);
        }
        if ("Mac OS X".equals(os)) {
            return resolveOnMac(pid);
        }
        if ("FreeBSD".equals(os)) {
            return resolveOnFreeBSD(pid);
        }
        if (os.toLowerCase().contains("windows")) {
            return resolveOnWindows(pid);
        }

        throw new UnsupportedOperationException("Unsupported Operating System " + os);
    }

    private static Cmdline resolveOnWindows(final int pid) {
        final String cmdline = new WinProcess(pid).getCommandLine();
        final IntByReference argc = new IntByReference();
        final Pointer argvPtr = Shell32.SHELL32.CommandLineToArgvW(new WString(cmdline), argc);
        final String[] procArgs = argvPtr.getStringArray(0, argc.getValue(), true);
        Kernel32.KERNEL32.LocalFree(argvPtr);
        return new Cmdline(Arrays.asList(procArgs));
    }

    private static Cmdline resolveOnLinux(final int pid) throws IOException {
        final String cmdline = readFile(new File("/proc/" + pid + "/cmdline"));
        return new Cmdline(Arrays.asList(cmdline.split("\0")));
    }

    private static Cmdline resolveOnFreeBSD(final int pid) {
        /* taken from sys/sysctl.h */
        final int CTL_KERN = 1;
        final int KERN_ARGMAX = 8;
        final int KERN_PROC = 14;
        final int KERN_PROC_ARGS = 7;

        final IntByReference ref = new IntByReference();
        final IntByReference sysctlArgMax = new IntByReference();
        final IntByReference size = new IntByReference();

        size.setValue(4);
        if (CLibrary.LIBC.sysctl(new int[]{CTL_KERN, KERN_ARGMAX}, 2, sysctlArgMax.getPointer(), size, NULL, ref) != 0) {
            throw new UnsupportedOperationException("Failed to sysctl kern.argmax");
        }

        final int argMax = sysctlArgMax.getValue();
        Memory m = new Memory(argMax);
        size.setValue(argMax);
        if (CLibrary.LIBC.sysctl(new int[]{CTL_KERN, KERN_PROC, KERN_PROC_ARGS}, 4, m, size, NULL, ref) != 0) {
            throw new UnsupportedOperationException();
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final List<String> args = new ArrayList<String>();
        byte ch;
        int offset = 0;
        while (offset < size.getValue()) {
            while ((ch = m.getByte(offset++)) != '\0') {
                baos.write(ch);
            }
            args.add(baos.toString());
            baos.reset();
        }
        return new Cmdline(args);
    }

    private static Cmdline resolveOnSolaris(final int pid) throws IOException {
        // /proc shows different contents based on the caller's memory model, so we need to know if we are 32 or 64.
        // 32 JVMs are the norm, so err on the 32bit side.
        final boolean areWe64 = "64".equals(System.getProperty("sun.arch.data.model"));
        final RandomAccessFile psinfo = new RandomAccessFile(new File("/proc/" + pid + "/psinfo"), "r");
        try {
            // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/uts/common/sys/procfs.h
            //typedef struct psinfo {
            //	int	pr_flag;	/* process flags */
            //	int	pr_nlwp;	/* number of lwps in the process */
            //	pid_t	pr_pid;	/* process id */
            //	pid_t	pr_ppid;	/* process id of parent */
            //	pid_t	pr_pgid;	/* process id of process group leader */
            //	pid_t	pr_sid;	/* session id */
            //	uid_t	pr_uid;	/* real user id */
            //	uid_t	pr_euid;	/* effective user id */
            //	gid_t	pr_gid;	/* real group id */
            //	gid_t	pr_egid;	/* effective group id */
            //	uintptr_t	pr_addr;	/* address of process */
            //	size_t	pr_size;	/* size of process image in Kbytes */
            //	size_t	pr_rssize;	/* resident set size in Kbytes */
            //	dev_t	pr_ttydev;	/* controlling tty device (or PRNODEV) */
            //	ushort_t	pr_pctcpu;	/* % of recent cpu time used by all lwps */
            //	ushort_t	pr_pctmem;	/* % of system memory used by process */
            //	timestruc_t	pr_start;	/* process start time, from the epoch */
            //	timestruc_t	pr_time;	/* cpu time for this process */
            //	timestruc_t	pr_ctime;	/* cpu time for reaped children */
            //	char	pr_fname[PRFNSZ];	/* name of exec'ed file */
            //	char	pr_psargs[PRARGSZ];	/* initial characters of arg list */
            //	int	pr_wstat;	/* if zombie, the wait() status */
            //	int	pr_argc;	/* initial argument count */
            //	uintptr_t	pr_argv;	/* address of initial argument vector */
            //	uintptr_t	pr_envp;	/* address of initial environment vector */
            //	char	pr_dmodel;	/* data model of the process */
            //	lwpsinfo_t	pr_lwp;	/* information for representative lwp */
            //} psinfo_t;

            // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/uts/common/sys/types.h
            // for the size of the various datatype.

            // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/cmd/ptools/pargs/pargs.c
            // for how to read this information

            psinfo.seek(8);
            if (adjust(psinfo.readInt()) != pid) {
                /* sanity check */
                throw new IOException("psinfo PID mismatch");
            }

            /* The following program computes the offset:
                    #include <stdio.h>
                    #include <sys/procfs.h>
                    int main() {
                      printf("psinfo_t = %d\n", sizeof(psinfo_t));
                      psinfo_t *x;
                      x = 0;
                      printf("%x\n", &(x->pr_argc));
                    }
             */

            psinfo.seek(areWe64 ? 0xEC : 0xBC);  // now jump to pr_argc

            final int argc = adjust(psinfo.readInt());
            final long argp = areWe64 ? adjust(psinfo.readLong()) : to64(adjust(psinfo.readInt()));
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest(String.format("argc=%d,argp=%X", argc, argp));
            }

            File asFile = new File("/proc/" + pid + "/as");
            if (areWe64) {
                // 32bit and 64bit basically does the same thing, but because the stream position
                // is computed with signed long, doing 64bit seek to a position bigger than Long.MAX_VALUE
                // requres some real hacking. Hence two different code path.
                // (RandomAccessFile uses Java long for offset, so it just can't get to anywhere beyond Long.MAX_VALUE)
                CLibrary.FILE fp = CLibrary.LIBC.fopen(asFile.getPath(), "r");
                try {
                    Cmdline args = new Cmdline();
                    Memory m = new Memory(8);
                    for (int n = 0; n < argc; n++) {
                        // read a pointer to one entry
                        seek64(fp, argp + n * 8);
                        if (LOGGER.isLoggable(FINEST))
                            LOGGER.finest(String.format("Seeked to %X", CLibrary.LIBC.ftell(fp)));

                        m.setLong(0, 0); // just to make sure failed read won't result in bogus value
                        CLibrary.LIBC.fread(m, 1, 8, fp);
                        long p = m.getLong(0);

                        args.add(readLine(fp, p, "argv[" + n + "]"));
                    }
                    return args;
                } finally {
                    CLibrary.LIBC.fclose(fp);
                }
            } else {
                RandomAccessFile as = new RandomAccessFile(asFile, "r");
                try {
                    Cmdline args = new Cmdline();
                    for (int n = 0; n < argc; n++) {
                        // read a pointer to one entry
                        as.seek(argp + n * 4);
                        int p = adjust(as.readInt());

                        args.add(readLine(as, p, "argv[" + n + "]"));
                    }
                    return args;
                } finally {
                    as.close();
                }
            }
        } finally {
            psinfo.close();
        }
    }

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
    private static Cmdline resolveOnMac(final int pid) {
        // local constants
        final int CTL_KERN = 1;
        final int KERN_ARGMAX = 8;
        final int KERN_PROCARGS2 = 49;
        final int sizeOfInt = Native.getNativeSize(int.class);
        final IntByReference ref = new IntByReference();


        final IntByReference argMaxRef = new IntByReference(0);
        final IntByReference size = new IntByReference(sizeOfInt);

        /* for some reason, I was never able to get sysctlbyname work. */
        /* if(LIBC.sysctlbyname("kern.argmax", argmaxRef.getPointer(), size, NULL, _)!=0) */
        if (CLibrary.LIBC.sysctl(new int[]{CTL_KERN, KERN_ARGMAX}, 2, argMaxRef.getPointer(), size, NULL, ref) != 0) {
            throw new UnsupportedOperationException("Failed to get kernl.argmax: " + CLibrary.LIBC.strerror(Native.getLastError()));
        }

        final int argmax = argMaxRef.getValue();
        LOGGER.fine("argmax=" + argmax);

        class StringArrayMemory extends Memory {
            private long offset = 0;

            StringArrayMemory(final long l) {
                super(l);
            }

            int readInt() {
                final int r = getInt(offset);
                offset += sizeOfInt;
                return r;
            }

            byte peek() {
                return getByte(offset);
            }

            String readString() {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte ch;
                while ((ch = getByte(offset++)) != '\0') {
                    baos.write(ch);
                }
                return baos.toString();
            }

            void skip0() {
                // skip trailing '\0's
                while (getByte(offset) == '\0') {
                    offset++;
                }
            }
        }

        final StringArrayMemory m = new StringArrayMemory(argmax);
        size.setValue(argmax);
        if (CLibrary.LIBC.sysctl(new int[]{CTL_KERN, KERN_PROCARGS2, pid}, 3, m, size, NULL, ref) != 0) {
            throw new UnsupportedOperationException("Failed to obtain ken.procargs2: " + CLibrary.LIBC.strerror(Native.getLastError()));
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

        final Cmdline args = new Cmdline();
        final int nargs = m.readInt();
        m.readString(); // exec path
        for (int i = 0; i < nargs; i++) {
            m.skip0();
            args.add(m.readString());
        }

        /*-
         * this is how you can read environment variables
         * List<String> lst = new ArrayList<String>();
         * while(m.peek()!=0)
         *     lst.add(m.readString());
         */
        return args;
    }
    /**
     * Seek to the specified position. This method handles offset bigger than {@link Long#MAX_VALUE} correctly.
     *
     * @param upos This value is interpreted as unsigned 64bit integer (even though it's typed 'long')
     */
    private static void seek64(CLibrary.FILE fp, long upos) {
        CLibrary.LIBC.fseek(fp, 0, 0); // start at the beginning
        while (upos < 0) {
            long chunk = Long.MAX_VALUE;
            upos -= chunk;
            CLibrary.LIBC.fseek(fp, chunk, 1);
        }
        CLibrary.LIBC.fseek(fp, upos, 1);
    }

    /**
     * {@link java.io.DataInputStream} reads a value in big-endian, so
     * convert it to the correct value on little-endian systems.
     */
    private static int adjust(final int i) {
        return IS_LITTLE_ENDIAN ? ((i << 24) | ((i << 8) & 0x00FF0000) | ((i >> 8) & 0x0000FF00) | (i >>> 24)) : i;
    }

    private static long adjust(final long i) {
        return IS_LITTLE_ENDIAN
                ? ((i << 56)
                | ((i << 40) & 0x00FF000000000000L)
                | ((i << 24) & 0x0000FF0000000000L)
                | ((i << 8) & 0x000000FF00000000L)
                | ((i >> 8) & 0x00000000FF000000L)
                | ((i >> 24) & 0x0000000000FF0000L)
                | ((i >> 40) & 0x000000000000FF00L)
                | (i >> 56))
                : i;
    }

    /**
     * int to long conversion with zero-padding.
     */
    private static long to64(final int i) {
        return i & 0xFFFFFFFFL;
    }

    private static String readLine(RandomAccessFile as, int p, String prefix) throws IOException {
        if (LOGGER.isLoggable(FINEST))
            LOGGER.finest(String.format("Reading %s at %X", prefix, p));

        as.seek(to64(p));
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int ch, i = 0;
        while ((ch = as.read()) > 0) {
            if ((++i) % 100 == 0 && LOGGER.isLoggable(FINEST))
                LOGGER.finest(prefix + " is so far " + buf.toString());

            buf.write(ch);
        }
        String line = buf.toString();
        if (LOGGER.isLoggable(FINEST))
            LOGGER.finest(prefix + " was " + line);
        return line;
    }

    private static String readLine(CLibrary.FILE as, long p, String prefix) throws IOException {
        if (LOGGER.isLoggable(FINEST))
            LOGGER.finest(String.format("Reading %s at %X", prefix, p));

        seek64(as, p);
        Memory m = new Memory(1);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int i = 0;
        while (true) {
            if (CLibrary.LIBC.fread(m, 1, 1, as) == 0) break;
            byte b = m.getByte(0);
            if (b == 0) break;

            if ((++i) % 100 == 0 && LOGGER.isLoggable(FINEST))
                LOGGER.finest(prefix + " is so far " + buf.toString());

            buf.write(b);
        }
        String line = buf.toString();
        if (LOGGER.isLoggable(FINEST))
            LOGGER.finest(prefix + " was " + line);
        return line;
    }

    /**
     * Reads the entire file.
     */
    private static String readFile(File f) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fin = new FileInputStream(f);
        try {
            int sz;
            byte[] buf = new byte[1024];

            while ((sz = fin.read(buf)) >= 0) {
                baos.write(buf, 0, sz);
            }

            return baos.toString();
        } finally {
            fin.close();
        }
    }
}