package freework.util;

import freework.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * 文件操作工具类
 *
 * @author vacoor
 */
public abstract class FileUtils {
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char DOT = '.';

    /**
     * 获取文件名称
     *
     * @param filename 文件名或路径
     * @return 文件名称
     */
    public static String getName(String filename) {
        if (null == filename) {
            return null;
        }
        return filename.substring(getParentLength(filename));
    }

    /**
     * 获取文件的扩展名
     *
     * @param filename  文件名或路径
     * @param returnDot 是否返回带"."的扩展名
     * @return 扩展名
     */
    public static String getExtension(String filename, boolean returnDot) {
        int index = indexOfExtension(filename);
        return -1 < index ? filename.substring(returnDot ? index : index + 1) : "";
    }

    /**
     * 获取不包含扩展名的文件名
     *
     * @param filename 文件名
     * @return 不包含扩展名的文件名
     */
    public static String getNameWithoutExtension(String filename) {
        String name = getName(filename);
        int index = indexOfExtension(name);
        return null != name && -1 < index ? name.substring(0, index) : name;
    }

    /**
     * 获取文件路径的父路径
     *
     * @param filename 文件路径
     * @return 父路径
     */
    public static String getParent(String filename) {
        if (null == filename) {
            return null;
        }
        return filename.substring(0, getParentLength(filename));
    }

    /**
     * 获取父路径的长度
     *
     * @param path 文件路径
     * @return 父路径长度
     */
    private static int getParentLength(String path) {
        if (null == path) {
            return 0;
        }

        int lastPathPos = indexOfLastPathSeparator(path);

        // 去除结尾的 "/"
        while (-1 < lastPathPos && lastPathPos == path.length() - 1) {
            path = path.substring(0, path.length() - 1);
            lastPathPos = indexOfLastPathSeparator(path);
        }
        return lastPathPos + 1;
    }

    /**
     * 获取扩展名的索引
     *
     * @param filename 文件名或路径
     * @return 扩展名的索引
     */
    public static int indexOfExtension(String filename) {
        if (null == filename) {
            return -1;
        }
        int lastPathPos = indexOfLastPathSeparator(filename);
        int lastDotPos = filename.lastIndexOf(DOT);
        return lastPathPos > lastDotPos ? -1 : lastDotPos;
    }

    /**
     * 获取最后一个文件分隔符的索引
     *
     * @param filename 文件名或路径
     * @return 最后一个文件分隔符的索引
     */
    public static int indexOfLastPathSeparator(String filename) {
        if (null == filename) {
            return -1;
        }
        int unixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int windowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(unixPos, windowsPos);
    }

    /**
     * 创建父目录
     *
     * @param file 要创建父路径的文件
     * @return 父目录是否存在
     */
    public static boolean createParentDirectory(File file) {
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (null != parentFile) {
                return createDirectory(parentFile);
            }
        }
        return true;
    }

    /**
     * 创建目录
     *
     * @param path 路径
     * @return 最后目录是否存在
     */
    public static boolean createDirectory(File path) {
        return path.isDirectory() || path.mkdirs();
    }

    public static void ensureDirectory(File path) throws IOException {
        if (!path.exists() && !createDirectory(path)) {
            throw new IOException("Can't create directory: " + path);
        } else if (path.exists() && !path.isDirectory()) {
            throw new IOException("Path exists and is not a directory: " + path);
        }
    }

    public static void copyDirectory(File from, File to) throws IOException {
        if (!from.exists() || !from.isDirectory()) {
            throw new IOException("Path not exists or is not a directory: " + from);
        }
        ensureDirectory(to);
        File[] files = from.listFiles();
        if (null != files) {
            for (File file : files) {
                if (file.isDirectory()) {
                    copyDirectory(file, new File(to, file.getName()));
                } else {
                    copy(file, new File(to, file.getName()));
                }
            }
        }
    }

    public static void delete(File pFile, boolean force, boolean recursive) throws IOException {
        if (!pFile.exists()) {
            return;
        }
        if (pFile.isDirectory()) {
            File[] files = pFile.listFiles();
            files = null != files ? files : new File[0];
            if (!force && 0 < files.length) {
                throw new IOException("You cannot delete non-empty directory, use force=true to overide");
            }

            for (File f : files) {
                String name = f.getName();
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }
                if (!recursive && f.isDirectory()) {
                    throw new IOException("Directory has contents, cannot delete without recurse=true");
                }
                delete(new File(pFile, name), force, recursive);
            }
        }
        if (!pFile.delete()) {
            throw new IOException("cannot delete file: " + pFile);
        }
    }

    /**
     * 拷贝文件
     *
     * @param from 源
     * @param to   目标
     * @throws IOException
     */
    public static void copy(File from, File to) throws IOException {
        IOUtils.flow(asInputStream(from), asOutputStream(to), true, true);
    }


    /**
     * 创建给定文件的 Reader
     *
     * @param file    文件
     * @param charset 字符编码
     * @return reader
     * @throws IOException
     */
    public static Reader asReader(File file, Charset charset) throws IOException {
        return new InputStreamReader(asInputStream(file), charset);
    }

    /**
     * 创建 给定文件的输入流
     *
     * @param file 目标文件
     * @return 目标文件的输入流
     * @throws IOException
     */
    public static InputStream asInputStream(File file) throws IOException {
        return new FileInputStream(file);
    }

    /**
     * 创建给定文件的 Writer
     *
     * @param file    目标文件
     * @param charset 目标文件字符编码
     * @return 目标文件的 Writer
     * @throws IOException
     */
    public static Writer asWriter(File file, Charset charset) throws IOException {
        return new OutputStreamWriter(asOutputStream(file), charset);
    }

    /**
     * 创建给定文件的输出流
     *
     * @param file 目标文件
     * @return 目标文件的输出流
     * @throws IOException
     */
    public static OutputStream asOutputStream(File file) throws IOException {
        return new FileOutputStream(file);
    }

    /**
     * 获取给定文件的字节
     *
     * @param file 目标文件
     * @return 字节
     * @throws IOException
     */
    public static byte[] loadBytes(File file) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        InputStream is = asInputStream(file);
        IOUtils.flow(is, bytes, true, true);
        return bytes.toByteArray();
    }

    /**
     * 获取给定文件的文本内容
     *
     * @param file    目标文件
     * @param charset 字符编码
     * @return 目标文件的文本内容
     * @throws IOException
     */
    public static String loadText(File file, Charset charset) throws IOException {
        StringWriter writer = new StringWriter();
        Reader reader = asReader(file, charset);
        IOUtils.flow(reader, writer, true, true);
        return writer.toString();
    }

    private FileUtils() {}
}
