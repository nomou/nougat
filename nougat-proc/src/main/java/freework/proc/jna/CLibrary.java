package freework.proc.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;

/**
 * GNU C library.
 * <p/>
 * Not available on all platforms (such as Linux/PPC, IBM mainframe, etc.), so the caller should recover gracefully
 * in case of {@link LinkageError}. See HUDSON-4820.
 *
 * @author Kohsuke Kawaguchi
 */
public interface CLibrary extends Library {
    CLibrary LIBC = (CLibrary) Native.loadLibrary("c", CLibrary.class);

    /**
     * 中断信号(同Ctrl+C).
     */
    int SIGINT = 2;

    /**
     * 正常退出信号.
     */
    int SIGQUIT = 3;

    /**
     * 强制终止信号.
     */
    int SIGKILL = 9;

    /**
     * 终止信号.
     */
    int SIGTERM = 15;

    /**
     * 暂停信号.
     */
    int SIGCONT = 19;

    /**
     * 读取文件描述符.
     */
    int F_GETFD = 1;

    /**
     * 设置文件描述符.
     */
    int F_SETFD = 2;

    /**
     * 设置close-on-exec标志.
     */
    int FD_CLOEXEC = 1;

    int fork();

    /**
     * 发送信号到指定进程.
     *
     * @param pid    进程ID
     * @param signum 信号值
     * @return 成功执行返回0, 失败返回-1
     */
    int kill(final int pid, final int signum);

    /**
     * 获取当前进程PID.
     *
     * @return 当前进程PID
     */
    int getpid();

    /**
     * 返回子进程文件描述符表的项数.
     *
     * @return 子进程文件描述符表的项数
     */
    int getdtablesize();

    /**
     * 停止执行当前的进程，并且以给定应用进程替换被停止执行的进程, 进程ID没有改变.
     *
     * @param path 被执行的应用程序
     * @param args 传递给应用程序的参数
     * @return 如果执行失败返回-1, 否则应该永远不返回
     */
    int execv(final String path, final StringArray args);

    /**
     * 获取错误标号对应的错误信息.
     *
     * @param errno 错误标号
     * @return 错误标号对应的错误信息
     */
    String strerror(final int errno);

    /**
     * 文件描述符操作.
     *
     * @param fd      文件描述符
     * @param command 操作指令
     */
    int fcntl(final int fd, final int command);

    /**
     * 文件描述符操作.
     *
     * @param fd      文件描述符
     * @param command 操作指令
     * @param flags   标志集
     */
    int fcntl(int fd, int command, int flags);


    int sysctl(int[] mib, int nameLen, Pointer oldp, IntByReference oldlenp, Pointer newp, IntByReference newlen);

    /**
     * 打开文件.
     *
     * @param fileName 文件名称
     * @param mode     打开模式
     * @return 文件
     */
    FILE fopen(final String fileName, final String mode);

    /**
     * 将 file 指针指向以whence为基准, 偏移offset个字节的位置.
     *
     * @param file   文件指针
     * @param offset 偏移量
     * @param whence 基准量
     * @return 操作结果
     */
    int fseek(final FILE file, final long offset, final int whence);

    /**
     * 获取文件指针相对文件首的偏移量.
     *
     * @param file 文件指针
     * @return 偏移量
     */
    long ftell(final FILE file);

    /**
     * 从文件中读取count个字节到缓冲区.
     *
     * @param buf   缓冲区指针
     * @param size  缓冲区对象的字节
     * @param count 读取长度
     * @param file  文件指针
     * @return
     */
    int fread(final Pointer buf, final int size, final int count, final FILE file);

    /**
     * 关闭文件.
     *
     * @param file 文件
     * @return 操作结果
     */
    int fclose(final FILE file);

    /**
     * 文件.
     */
    class FILE extends PointerType {
        public FILE() {
        }

        public FILE(final Pointer pointer) {
            super(pointer);
        }
    }
}