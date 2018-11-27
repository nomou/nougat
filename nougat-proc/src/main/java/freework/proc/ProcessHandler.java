package freework.proc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 进程信息处理器, 配合 {@link ProcessStarter} 使用.
 *
 * @author vacoor
 */
public interface ProcessHandler {

    /**
     * 处理进程输入
     *
     * @param output 输出到进程输入的输出流
     * @throws IOException
     */
    void processInput(OutputStream output) throws IOException;

    /**
     * 处理进程输出
     *
     * @param input 获取进程输出的输入流
     * @throws IOException
     */
    void processOutput(InputStream input) throws IOException;

    /**
     * 处理进程错误输出
     *
     * @param errorInput 获取进程错误输出的输入流
     * @throws IOException
     */
    void processError(InputStream errorInput) throws IOException;

    /**
     * 处理进程执行结束
     *
     * @param command  进程的命令行参数
     * @param exitCode 进程的退出返回值
     * @throws IOException
     */
    void finished(String[] command, int exitCode);

}
