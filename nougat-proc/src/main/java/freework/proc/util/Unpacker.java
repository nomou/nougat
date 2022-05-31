package freework.proc.util;

import freework.codec.Hex;
import freework.crypto.digest.Hash;
import freework.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.security.DigestOutputStream;

public class Unpacker {
    private static final String UNPACK_TO_PARENT_DIR = "unpack.to.parent.dir";

    public static File unpack(final URL source) {
        if (null == source) {
            return null;
        }
        final String url = source.toExternalForm();
        final boolean unpackToParent = Boolean.parseBoolean(System.getProperty(UNPACK_TO_PARENT_DIR, "true"));
        if (unpackToParent && (url.startsWith("jar:") || url.startsWith("wsjar:"))) {
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
                final String filename = hash(source) + '.' + filename(url);
                final File targetFile = new File(jarFile.getParentFile(), filename);
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
            final File tempFile = File.createTempFile("foo", filename(url));
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
            IOUtils.flow(source.openStream(), new FileOutputStream(target), true, true);
        } catch (final IOException ioe) {
            throw new IllegalStateException("failed to copy resource " + source + ": " + ioe.getMessage(), ioe);
        }
    }

    private static String hash(final URL source) {
        try {
            final DigestOutputStream out = Hash.nopOut(Hash.Algorithm.MD5);
            IOUtils.flow(source.openStream(), out, true, true);
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

    public static void main(String[] args) {
        final URL url = Unpacker.class.getResource("/com/sun/jna/darwin/libjnidispatch.jnilib");
        final File unpackFile = unpack(url);
        System.out.println(unpackFile);
    }
}