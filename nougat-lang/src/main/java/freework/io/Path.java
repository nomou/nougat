package freework.io;

import freework.util.StringUtils2;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Path strings use slash as the directory separator.
 * A path string is absolute if it begins with a slash.
 *
 * @since 1.0
 */
public class Path implements Comparable, Cloneable {
    /**
     * The platform whether is windows.
     */
    private static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");

    /**
     * Pre-compiled regular expressions to detect path formats.
     */
    private static final Pattern HAS_DRIVE_LETTER_SPECIFIER = Pattern.compile("^/?[a-zA-Z]:");

    /**
     * The directory separator string.
     */
    public static final String SEPARATOR = "/";

    /**
     * The directory separator char.
     */
    public static final char SEPARATOR_CHAR = '/';

    /**
     * The current folder path.
     */
    public static final String CUR_DIR = "./";

    /**
     * The name of current folder.
     */
    public static final String CUR_DIR_NAME = ".";

    /**
     * The parent path.
     */
    public static final String PARENT_DIR = "../";

    /**
     * The name of parent path.
     */
    public static final String PARENT_DIR_NAME = "..";

    /**
     * The normalized path.
     */
    private String path;

    /**
     * Creates a path from the path string.
     *
     * @param pathString the path string
     */
    protected Path(final String pathString) throws IllegalArgumentException {
        if (null == pathString) {
            throw new IllegalArgumentException("Can not create a Path from a null string");
        }

        String normalizedPathString = normalize(pathString);
        if (hasWindowsDrive(normalizedPathString) && normalizedPathString.charAt(0) != SEPARATOR_CHAR) {
            // insert '/' before the window drive
            normalizedPathString = SEPARATOR + normalizedPathString;
        }

        if (!WINDOWS && normalizedPathString.charAt(0) != SEPARATOR_CHAR && !CUR_DIR.equals(normalizedPathString) && !PARENT_DIR.equals(normalizedPathString)) {
            // insert './' before the java/unix path.
            normalizedPathString = CUR_DIR + normalizedPathString;
        }
        this.path = normalizedPathString;
    }

    /**
     * Gets the name of this path.
     *
     * @return the name of this path
     */
    public String getName() {
        final int slash = path.lastIndexOf(SEPARATOR);
        return path.substring(slash + 1);
    }

    /**
     * Gets the normalized path of this path.
     *
     * @return the normalized path
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
     * Gets the parent path of this path.
     *
     * @return null if this path is root, otherwise the parent path
     */
    public Path getParent() {
        final int lastSlash = path.lastIndexOf(SEPARATOR_CHAR);
        final int start = hasWindowsDrive(path) ? 3 : 0;

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
     * Returns whether the path is absolute path.
     *
     * @return true if this path is absolute
     */
    public boolean isAbsolute() {
        final int start = hasWindowsDrive(path) ? 3 : 0;
        return path.startsWith(SEPARATOR, start);
    }

    /**
     * Returns whether the path is root.
     *
     * @return true if this path is root
     */
    public boolean isRoot() {
        return null == getParent();
    }

    /**
     * Gets the depth of this path.
     *
     * @return the depth of this path
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
     * Returns whether this path starts with the given path.
     *
     * @param path the target path
     * @return true if this path starts with the given path
     */
    public boolean startsWith(final Path path) {
        return startsWith(path.path);
    }

    /**
     * Returns whether this path starts with the given path.
     *
     * @param path the target path
     * @return true if this path starts with the given path
     */
    public boolean startsWith(final String path) {
        final String me = normalize(this.path);
        final String other = normalize(path);
        return StringUtils2.startsWith(me, other, WINDOWS);
    }

    /**
     * Returns whether this path ends with the given path.
     *
     * @param path the target path
     * @return true if this path ends with the given path
     */
    public boolean endsWith(final Path path) {
        return endsWith(path.path);
    }

    /**
     * Returns whether this path ends with the given path.
     *
     * @param path the target path
     * @return true if this path ends with the given path
     */
    public boolean endsWith(final String path) {
        final String me = normalize(this.path);
        final String other = normalize(path);
        return StringUtils2.endsWith(me, other, WINDOWS);
    }

    /**
     * Returns the path after appending the suffix to this path.
     *
     * @param suffix the suffix
     * @return new-path
     */
    public Path suffix(final String suffix) {
        return get(getParent(), getName() + suffix);
    }

    /**
     * Gets the child path under the this path.
     *
     * @param child the name or relative path of this path
     * @return the child path
     */
    public Path resolve(final String child) {
        return get(this, child);
    }

    /**
     * Gets the child path under the this path.
     *
     * @param children the name or relative path of this path
     * @return the child path
     */
    public Path resolve(final String... children) {
        Path parent = new Path(this.path);
        for (final String child : children) {
            parent = parent.resolve(child);
        }
        return parent;
    }

    /**
     * Gets the child path under the this path.
     *
     * @param child the name or relative path of this path
     * @return the child path
     */
    public Path resolve(final Path child) {
        return get(this, child);
    }

    /**
     * Get the relative path of this path relative to the given path.
     *
     * @param base the base path
     * @return the relative path
     */
    public Path relativize(final Path base) {
        return new Path(relativize(base.getPath()));
    }

    /**
     * Gets the relative path of this path relative to the given path.
     *
     * @param base the base path
     * @return the relative path
     */
    public String relativize(final String base) {
        return relativize(getPath(), base);
    }

    /**
     * Gets as java.io.File.
     *
     * @return the file
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


    /* *********************************
     *          STATIC METHODS
     * ******************************* */

    /**
     * Creates an path object, using the given parent and child paths.
     *
     * @param parent the parent path
     * @param child  the child path
     * @return the child path object
     */
    public static Path get(final String parent, final String child) {
        return get(new Path(parent), new Path(child));
    }

    /**
     * Creates an path object, using the given parent and child paths.
     *
     * @param parent the parent path
     * @param child  the child path
     * @return the child path object
     */
    public static Path get(final Path parent, final String child) {
        return get(parent, new Path(child));
    }

    /**
     * Creates an path object, using the given parent and child paths.
     *
     * @param parent the parent path
     * @param child  the child path
     * @return the child path object
     */
    public static Path get(final String parent, final Path child) {
        return get(new Path(parent), child);
    }

    /**
     * Creates an path object, using the given parent and child paths.
     *
     * @param parent the parent path
     * @param child  the child path
     * @return the child path object
     */
    public static Path get(final Path parent, final Path child) {
        final String parentPath = parent.getPath();
        final String childPath = child.getPath();
        return get(normalize(resolve(parentPath, childPath)));
    }

    /**
     * Creates an path object, using the given path string.
     *
     * @param path the path string
     * @return the path object
     */
    public static Path get(final String path) {
        return new Path(path);
    }

    /**
     * Creates an path object, using the given parent and child paths.
     * <p>
     * base: a/b, child: c/d, return: a/b/c/d <br/>
     * base: a/b, child: /c/d, return: a/b/c/d <br/>
     *
     * @param base  the parent path
     * @param child the child path
     * @return the child path object
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
     * Gets the relative path of the path relative to the given base path.
     *
     * @param path the path
     * @param base the base path
     * @return the relative path
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
     * Normalize a path that may have relative values ("/./", "/../", and so on ) it it.
     *
     * @param path path to be normalized
     * @return normalized path
     */
    public static String normalize(final String path) {
        return normalize(path, true);
    }

    /**
     * Normalize a path that may have relative values ("/./", "/../", and so on ) it it.
     *
     * @param path             path to be normalized
     * @param replaceBackSlash true if should '\\' be replaced with '/'
     * @return normalized path
     */
    private static String normalize(String path, final boolean replaceBackSlash) {
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
                // Trying to go outside our context
                return (null);
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
     * Returns whether the given path string contains a windows drive.
     *
     * @param path the path string
     * @return true if the given path string contains a windows drive
     */
    private static boolean hasWindowsDrive(final String path) {
        return (WINDOWS && HAS_DRIVE_LETTER_SPECIFIER.matcher(path).find());
    }
}
