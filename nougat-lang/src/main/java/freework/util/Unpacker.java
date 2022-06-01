package freework.util;

import freework.codec.Hex;
import freework.crypto.digest.Hash;
import freework.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.security.DigestOutputStream;

public class Unpacker {

    public static File unpackAsTempFile(final URL source) {
        return unpackToDirectory(source, null);
    }

    public static File unpackToDirectory(final URL source, final File directory) {
        return unpackToDirectory(source, false, directory);
    }

    public static File unpackToJarDirectory(final URL source) {
        return unpackToDirectory(source, true, null);
    }

    private static File unpackToDirectory(final URL source, final boolean useJarDirectory, final File directory) {
        if (null == source) {
            throw new IllegalArgumentException("source must not be null");
        }
        final String url = source.toExternalForm();
        final boolean hasDirectory = useJarDirectory && null != directory;
        if (hasDirectory && (url.startsWith("jar:") || url.startsWith("wsjar:"))) {
            /*-
             * jar 文件且是解压到固定目录.
             * jar:file:/path.jar!path_in_jar
             */
            final int index = url.lastIndexOf('!');
            String jarPath = url.substring(url.indexOf(':') + 1, index);
            while (jarPath.startsWith("/")) {
                jarPath = jarPath.substring(1);
            }

            if (jarPath.startsWith("file:")) {
                jarPath = jarPath.substring(5);
                if (jarPath.startsWith("///")) {
                    /*-
                     * JDK on Unix uses 'file:/home/foo/bar', whereas RFC 'file:///home/foo/bar'
                     */
                    jarPath = jarPath.substring(2);
                } else if (jarPath.startsWith("//")) {
                    /*-
                     * this indicates file://host/path-in-host format,
                     * Windows maps UNC path to this.
                     * On Unix, there's no well defined semantics for this.
                     */
                }

                final File jarFile = new File(decode(jarPath));
                final String nameToUse = hash(source) + '.' + filename(url);
                final File targetFile = new File(null != directory ? directory : jarFile.getParentFile(), nameToUse);
                if (!targetFile.exists()) {
                    copy(source, targetFile);
                }
                return targetFile;
            }
        } else if (url.startsWith("file:")) {
            /*-
             * 文件系统.
             */
            try {
                return new File(source.toURI());
            } catch (final URISyntaxException e) {
                return new File(source.getPath());
            }
        }
        /*-
         * 非文件系统, 且不是解压到固定目录.
         */
        try {
            final File tempFile = File.createTempFile("unpack", filename(url));
            tempFile.deleteOnExit();
            copy(source, tempFile);
            return tempFile;
        } catch (final IOException ioe) {
            throw new IllegalStateException("failed to copy resource " + source + ": " + ioe.getMessage(), ioe);
        }
    }

    private static String filename(final String path) {
        final int i = path.lastIndexOf('/');
        return path.substring(i + 1);
    }

    private static void copy(final URL source, final File target) {
        try {
            final File directory = target.getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IllegalStateException("Cannot create directory: " + directory);
            }
            IOUtils.flow(source.openStream(), new FileOutputStream(target), true, true);
        } catch (final IOException ioe) {
            throw new IllegalStateException("failed to copy resource " + source + ": " + ioe.getMessage(), ioe);
        }
    }

    private static String hash(final URL source) {
        try {
            final DigestOutputStream out = Hash.digestOut(Hash.Algorithm.MD5);
            IOUtils.flow(source.openStream(), out, true, true);
            return Hex.encode(out.getMessageDigest().digest());
        } catch (final IOException ioe) {
            throw new IllegalStateException("failed to checksum " + source + ": " + ioe.getMessage(), ioe);
        }
    }

    private static String hash(final File source) {
        try {
            final DigestOutputStream out = Hash.digestOut(Hash.Algorithm.MD5);
            IOUtils.flow(new FileInputStream(source), out, true, true);
            return Hex.encode(out.getMessageDigest().digest());
        } catch (final IOException ioe) {
            throw new IllegalStateException("failed to checksum " + source + ": " + ioe.getMessage(), ioe);
        }
    }

    private static String decode(final String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(StandardCharsets.UTF_8.name());
        }
    }
}