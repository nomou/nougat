package freework.proc.handle.unix;

import com.sun.jna.Memory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SolarisHandle extends UnixHandle {
    private static final boolean IS_LITTLE_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"));
    private static final SolarisHandle JVM = of(getJvmPid());

    private SolarisHandle(final int pid) {
        super(pid);
    }

    private SolarisHandle(final int pid, final Process process) {
        super(pid, process);
    }

    @Override
    protected Info info(final int pid) {
        // /proc shows different contents based on the caller's memory model, so we need to know if we are 32 or 64.
        // 32 JVMs are the norm, so err on the 32bit side.
        final boolean areWe64 = "64".equals(System.getProperty("sun.arch.data.model"));
        final List<String> args = new ArrayList<>();
        try {
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

                File asFile = new File("/proc/" + pid + "/as");
                if (areWe64) {
                    // 32bit and 64bit basically does the same thing, but because the stream position
                    // is computed with signed long, doing 64bit seek to a position bigger than Long.MAX_VALUE
                    // requres some real hacking. Hence two different code path.
                    // (RandomAccessFile uses Java long for offset, so it just can't get to anywhere beyond Long.MAX_VALUE)
                    LibraryC.FILE fp = LibraryC.LIBC.fopen(asFile.getPath(), "r");
                    try {
                        Memory m = new Memory(8);
                        for (int n = 0; n < argc; n++) {
                            // read a pointer to one entry
                            seek64(fp, argp + n * 8);

                            m.setLong(0, 0); // just to make sure failed read won't result in bogus value
                            LibraryC.LIBC.fread(m, 1, 8, fp);
                            long p = m.getLong(0);

                            args.add(readLine(fp, p, "argv[" + n + "]"));
                        }
                    } finally {
                        LibraryC.LIBC.fclose(fp);
                    }
                } else {
                    RandomAccessFile as = new RandomAccessFile(asFile, "r");
                    try {
                        for (int n = 0; n < argc; n++) {
                            // read a pointer to one entry
                            as.seek(argp + n * 4);
                            int p = adjust(as.readInt());

                            args.add(readLine(as, p, "argv[" + n + "]"));
                        }
                    } finally {
                        as.close();
                    }
                }
            } finally {
                psinfo.close();
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        final String[] procArgs = args.toArray(new String[args.size()]);
        return new InfoImpl(null, procArgs[0], Arrays.copyOfRange(procArgs, 1, procArgs.length));
    }

    /**
     * Seek to the specified position. This method handles offset bigger than {@link Long#MAX_VALUE} correctly.
     *
     * @param upos This value is interpreted as unsigned 64bit integer (even though it's typed 'long')
     */
    private static void seek64(LibraryC.FILE fp, long upos) {
        LibraryC.LIBC.fseek(fp, 0, 0); // start at the beginning
        while (upos < 0) {
            long chunk = Long.MAX_VALUE;
            upos -= chunk;
            LibraryC.LIBC.fseek(fp, chunk, 1);
        }
        LibraryC.LIBC.fseek(fp, upos, 1);
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
        as.seek(to64(p));
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int ch, i = 0;
        while ((ch = as.read()) > 0) {
            buf.write(ch);
        }
        String line = buf.toString();
        return line;
    }

    private static String readLine(LibraryC.FILE as, long p, String prefix) throws IOException {
        seek64(as, p);
        Memory m = new Memory(1);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int i = 0;
        while (true) {
            if (LibraryC.LIBC.fread(m, 1, 1, as) == 0) break;
            byte b = m.getByte(0);
            if (b == 0) break;

            buf.write(b);
        }
        String line = buf.toString();
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

    public static SolarisHandle current() {
        return JVM;
    }

    public static SolarisHandle of(final int pid) {
        return new SolarisHandle(pid);
    }

    public static SolarisHandle of(final Process process) {
        return new SolarisHandle(getUnixPid(process), process);
    }

}