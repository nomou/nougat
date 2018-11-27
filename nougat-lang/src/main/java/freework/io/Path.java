package freework.io;

import freework.util.StringUtils2;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 使用"/"来分隔的标准化路径工具类.
 *
 * @author vacoor
 */
public class Path implements Comparable, Cloneable {
    /**
     * 当前环境是否是 Windows.
     */
    private static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");

    /**
     * 路径格式检测正则表达式.
     */
    private static final Pattern HAS_DRIVE_LETTER_SPECIFIER = Pattern.compile("^/?[a-zA-Z]:");

    /**
     * 目录分隔符.
     */
    public static final String SEPARATOR = "/";

    /**
     * 目录分隔符字符.
     */
    public static final char SEPARATOR_CHAR = '/';

    /**
     * 当前目录.
     */
    public static final String CUR_DIR = "./";

    /**
     * 当前目录名称.
     */
    public static final String CUR_DIR_NAME = ".";

    /**
     * 父目录.
     */
    public static final String PARENT_DIR = "../";

    /**
     * 父目录名称.
     */
    public static final String PARENT_DIR_NAME = "..";

    /**
     * 内部标准化路径.
     */
    private String path;

    /**
     * 基于给定路径字符串构建一个路径对象.
     */
    protected Path(final String pathString) throws IllegalArgumentException {
        if (null == pathString) {
            throw new IllegalArgumentException("Can not create a Path from a null string");
        }

        String normalizedPathString = normalize(pathString);

        // Windows 驱动器前添加 "/".
        if (hasWindowsDrive(normalizedPathString) && normalizedPathString.charAt(0) != SEPARATOR_CHAR) {
            normalizedPathString = SEPARATOR + normalizedPathString;
        }

        // Linux 相对路径前添加 "./", (Linux 下相对路径中":"前内容会认为是schema, e.q. "a:b" will not be interpreted as scheme "a".)
        if (!WINDOWS && normalizedPathString.charAt(0) != SEPARATOR_CHAR && !CUR_DIR.equals(normalizedPathString) && !PARENT_DIR.equals(normalizedPathString)) {
            normalizedPathString = CUR_DIR + normalizedPathString;
        }

        this.path = normalizedPathString;
    }

    /**
     * 获取当前路径对应文件的名称.
     */
    public String getName() {
        final int slash = path.lastIndexOf(SEPARATOR);
        return path.substring(slash + 1);
    }

    /**
     * 获取标准化路径字符串.
     *
     * @return 路径字符串
     */
    public String getPath() {
        String path = this.path;

        final StringBuilder buffer = new StringBuilder();
        if (null != path) {
            if (path.indexOf(SEPARATOR_CHAR) == 0 && hasWindowsDrive(path)) {
                // remove slash before drive
                path = path.substring(1);
            } else if (2 < path.length() && path.indexOf(CUR_DIR) == 0) {
                path = path.substring(2);
            }
            buffer.append(path);
        }
        return buffer.toString();
    }

    /**
     * 获取当前路径的父路径.
     *
     * @return 如果当前路径是根路径返回null 否则返回父路径
     */
    public Path getParent() {
        int lastSlash = path.lastIndexOf(SEPARATOR_CHAR);
        int start = hasWindowsDrive(path) ? 3 : 0;

        // empty path or root
        if ((path.length() == start) || (lastSlash == start && path.length() == start + 1)) {
            return null;
        }
        String parent;
        if (lastSlash == -1) {
            parent = CUR_DIR_NAME;
        } else {
            final int end = hasWindowsDrive(path) ? 3 : 0;
            parent = path.substring(0, lastSlash == end ? end + 1 : lastSlash);
        }
        return new Path(parent);
    }

    /**
     * 获取当前路径是否是绝对路径.
     *
     * @return 如果是绝对路径返回true, 否则返回false
     */
    public boolean isAbsolute() {
        final int start = hasWindowsDrive(path) ? 3 : 0;
        return path.startsWith(SEPARATOR, start);
    }

    /**
     * 当前路径是否表示的是根路径.
     *
     * @return 如果是文件系统根返回true, 否则返回false
     */
    public boolean isRoot() {
        return getParent() == null;
    }

    /**
     * 获取当前路径的深度.
     *
     * @return 路径深度
     */
    public int depth() {
        int depth = 0;
        int slash = path.length() == 1 && path.charAt(0) == SEPARATOR_CHAR ? -1 : 0;
        while (slash != -1) {
            depth++;
            slash = path.indexOf(SEPARATOR, slash + 1);
        }
        return depth;
    }

    /**
     * 当前路径是否是以给定路径开始.
     *
     * @param path 目标路径
     * @return 如果开始于给定路径返回true, 否则返回false
     */
    public boolean startsWith(final Path path) {
        return startsWith(path.path);
    }

    /**
     * 当前路径是否是以给定路径开始.
     *
     * @param path 目标路径
     * @return 如果开始于给定路径返回true, 否则返回false
     */
    public boolean startsWith(final String path) {
        final String me = normalize(this.path);
        final String other = normalize(path);
        return StringUtils2.startsWith(me, other, WINDOWS);
    }

    /**
     * 当前路径是否是以给定路径结束.
     *
     * @param path 目标路径
     * @return 如果结束于给定路径返回true, 否则返回false
     */
    public boolean endsWith(final Path path) {
        return endsWith(path.path);
    }

    /**
     * 当前路径是否是以给定路径结束.
     *
     * @param path 目标路径
     * @return 如果结束于给定路径返回true, 否则返回false
     */
    public boolean endsWith(final String path) {
        final String me = normalize(this.path);
        final String other = normalize(path);
        return StringUtils2.endsWith(me, other, WINDOWS);
    }

    /**
     * 给当前文件添加后缀.
     */
    public Path suffix(final String suffix) {
        return get(getParent(), getName() + suffix);
    }

    /**
     * 获取当前路径下给定子路径的路径.
     *
     * @param child 子路径
     * @return 子路径
     */
    public Path resolve(final String child) {
        return get(this, child);
    }

    /**
     * 获取当前路径下给定后台路径的路径.
     *
     * @param children 后代路径
     * @return 最后一个后台的路径
     */
    public Path resolve(final String... children) {
        Path parent = new Path(this.path);
        for (final String child : children) {
            parent = parent.resolve(child);
        }
        return parent;
    }

    /**
     * 获取当前路径下给定子路径的路径.
     *
     * @param child 子路径
     * @return 子路径
     */
    public Path resolve(final Path child) {
        return get(this, child);
    }

    /**
     * 获取当前路径对基准路径的相对路径.
     *
     * @param base 基准路径
     * @return 相对路径
     */
    public Path relativize(final Path base) {
        return new Path(relativize(base.getPath()));
    }

    /**
     * 获取当前路径对基准路径的相对路径.
     *
     * @param base 基准路径
     * @return 相对路径
     */
    public String relativize(final String base) {
        return relativize(getPath(), base);
    }

    /**
     * 将当前路径转换为 File 对象.
     *
     * @return File
     */
    public File asFile() {
        return new File(getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Object other) {
        final Path that = (Path) other;
        return this.path.compareTo(that.path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Path)) {
            return false;
        }
        final Path that = (Path) other;
        return this.path.equals(that.path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getPath();
    }

    /* **************************************************************************************
     *
     * **************************************************************************************/

    /**
     * 解析给定父路径下的子路径.
     *
     * @param parent 父路径
     * @param child  子路径
     * @return 子路径的Path对象
     */
    public static Path get(final String parent, final String child) {
        return get(new Path(parent), new Path(child));
    }

    /**
     * 解析给定父路径下的子路径.
     *
     * @param parent 父路径
     * @param child  子路径
     * @return 子路径的Path对象
     */
    public static Path get(final Path parent, final String child) {
        return get(parent, new Path(child));
    }

    /**
     * 解析给定父路径下的子路径.
     *
     * @param parent 父路径
     * @param child  子路径
     * @return 子路径的Path对象
     */
    public static Path get(final String parent, final Path child) {
        return get(new Path(parent), child);
    }

    /**
     * 解析给定父路径下的子路径.
     *
     * @param parent 父路径
     * @param child  子路径
     * @return 子路径的Path对象
     */
    public static Path get(final Path parent, final Path child) {
        final String parentPath = parent.getPath();
        final String childPath = child.getPath();
        return get(normalize(resolve(parentPath, childPath)));
    }

    /**
     * 解析给定路径的路径对象.
     *
     * @param path 路径字符串
     * @return Path对象
     */
    public static Path get(String path) {
        return new Path(path);
    }

    /**
     * 解析给定路径下的子路径.
     * <p>
     * base: a/b, child: c/d, return: a/b/c/d <br/>
     * base: a/b, child: /c/d, return: a/b/c/d <br/>
     *
     * @param base  基准路径
     * @param child 子路径
     * @return 基于基准路径解析后的子路径
     */
    public static String resolve(final String base, String child) {
        if (null == base) {
            return child;
        }
        if (null == child) {
            return base;
        }
        child = !child.startsWith(SEPARATOR) ? child : child.substring(1);
        return base + (base.endsWith(SEPARATOR) ? child : (SEPARATOR + child));
    }

    /**
     * 获取路径于基准路径的相对路径.
     *
     * @param path 待处理路径
     * @param base 基准路径
     * @return 路径的相对路径
     */
    public static String relativize(final String path, final String base) {
        final String normalizedBase = normalize(base);
        final String normalizedPath = normalize(path);

        if (normalizedPath.length() < 1 || normalizedBase.length() < 1 || !normalizedPath.startsWith(normalizedBase)) {
            throw new IllegalArgumentException("path not has same parent path");
        }

        final StringBuilder buffer = new StringBuilder();
        final String[] basePairs = normalizedBase.split(SEPARATOR);
        final String[] pathPairs = normalizedPath.split(SEPARATOR);

        int i = 0;
        int len = Math.min(basePairs.length, pathPairs.length);

        for (; i < len && basePairs[i].equals(pathPairs[i]); i++) {
            // nothing
        }

        for (int j = i; j < basePairs.length; j++) {
            buffer.append(PARENT_DIR);
        }

        for (int j = i; j < pathPairs.length; j++) {
            if (pathPairs[j].length() < 1) {
                continue;
            }
            buffer.append(pathPairs[j]).append(SEPARATOR);
        }

        len = buffer.length();
        return 0 < len ? buffer.deleteCharAt(len - 1).toString() : "";
    }

    /**
     * Normalize a relative URI path that may have relative values ("/./",
     * "/../", and so on ) it it.  <strong>WARNING</strong> - This method is
     * useful only for normalizing application-generated paths.  It does not
     * try to perform security checks for malicious input.
     * Normalize operations were was happily taken from org.apache.catalina.util.RequestUtil in
     * Tomcat trunk, r939305
     *
     * @param path Relative path to be normalized
     * @return normalized path
     */
    public static String normalize(String path) {
        return normalize(path, true);
    }

    /**
     * Normalize a relative URI path that may have relative values ("/./",
     * "/../", and so on ) it it.  <strong>WARNING</strong> - This method is
     * useful only for normalizing application-generated paths.  It does not
     * try to perform security checks for malicious input.
     *
     * @param path             Relative path to be normalized
     * @param replaceBackSlash Should '\\' be replaced with '/'
     * @return normalized path
     */
    private static String normalize(String path, boolean replaceBackSlash) {
        if (path == null) {
            return null;
        }

        if (path.indexOf('/') == 0 && hasWindowsDrive(path)) {
            path = path.substring(1);
        }

        // Create a place for the normalized path
        String normalized = path;
        if (replaceBackSlash && normalized.indexOf('\\') >= 0) {
            normalized = normalized.replace('\\', '/');
        }

        if (normalized.equals("/.")) {
            return "/";
        }

        // Add a leading "/" if necessary
        /*
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        */

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            final int index = normalized.indexOf("/../");
            if (index < 0) {
                break;
            }
            if (index == 0) {
                return (null);  // Trying to go outside our context
            }

            final int index2 = normalized.lastIndexOf(SEPARATOR_CHAR, index - 1);
            if (index2 < 0) {
                normalized = normalized.substring(index + 4);
            } else {
                normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
            }
        }

        if (0 == normalized.length()) {
            normalized = "./";
        }
        // Return the normalized path that we have completed
        return (normalized);
    }

    /**
     * 是否是Windows系统的绝对路径.
     * <p>
     * 对于 Windows: C:/a/b 是绝对路径, C:a/b不是.
     *
     * @param pathString 需要判定的路径字符串.
     * @param slashed    是否是以"/"开头的Windows路径.
     * @return 如果包含驱动器则认为是绝对路径返回 true, 否则返回 false
     */
    public static boolean isWindowsAbsolutePath(final String pathString, final boolean slashed) {
        final int start = (slashed ? 1 : 0);

        return hasWindowsDrive(pathString) && pathString.length() >= (start + 3)
                && ((pathString.charAt(start + 2) == SEPARATOR_CHAR) || (pathString.charAt(start + 2) == '\\'));
    }

    /**
     * 是否包含 Windows 驱动器.
     *
     * @param path 待判定路径字符串
     * @return 是否包含 Windows 驱动器
     */
    private static boolean hasWindowsDrive(final String path) {
        return (WINDOWS && HAS_DRIVE_LETTER_SPECIFIER.matcher(path).find());
    }
}
