/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import freework.codec.Hex;
import freework.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author vacoor
 */
public abstract class Natives {
    public static final String EXTRACT_DIR = System.getProperty("java.tmp.dir");

    public static void loadLibrary(Class<?> clazz, String lib) {
        loadLibrary(clazz.getClassLoader(), lib);
    }

    /**
     * @param loader classLoader
     * @param lib    library name or library path
     */
    public static void loadLibrary(final ClassLoader loader, final String lib) {
        // first, try original lib
        URL url = loader.getResource(lib);

        // next, try map name: *.so, *.dll, *.dylib, *.jnilib
        if (null == url) {
            String normalized = lib.replace("\\", "/");
            int index = normalized.lastIndexOf("/");
            String name = System.mapLibraryName(normalized.substring(index + 1));
            name = 0 > index ? name : normalized.substring(0, index) + "/" + name;

            url = loader.getResource(name);

            if (null == url) {
                // mac mapLibraryName("native") --&gt; native.dylib or libnative.jnilib
                url = loader.getResource(name.replaceAll("\\.dylib$", ".jnilib"));
            }
        }

        // classLoader find library
        if (null != url) {
            String res = url.toExternalForm();

            // in file system
            if (res.startsWith("file:")) {
                File file;
                try {
                    file = new File(url.toURI());
                } catch (URISyntaxException e) {
                    file = new File(url.getPath());
                }

                loadLibrary(file);
                return;
            }

            // in jar or other
            try {
                String md5 = md5(url.openStream());
                File file = new File(EXTRACT_DIR, System.mapLibraryName(md5));

                if (!file.exists() || md5.equals(md5(file))) {
                    File folder = file.getParentFile();

                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    file.deleteOnExit();

                    IOUtils.flow(url.openStream(), new FileOutputStream(file), true, true);
                }

                loadLibrary(file);
            } catch (IOException e) {
                throw new Error(e);
            } catch (LinkageError err) {
                // LOG
            }
        }

        try {
            // try java.library.path
            System.loadLibrary(lib);
        } catch (Throwable cause) {
            UnsatisfiedLinkError error = new UnsatisfiedLinkError("Unable to load native library: " + lib);
            error.initCause(cause);
            throw error;
        }
    }

    /**
     * Loads a library with a precaution for multi-classloader situation.
     */
    private static void loadLibrary(File libFile) throws LinkageError {
        try {
            System.load(libFile.getAbsolutePath());
        } catch (LinkageError e) {
            /*
             * see http://forum.java.sun.com/thread.jspa?threadID=618431&messageID=3462466
             * if another ClassLoader loaded winp, loading may fail
             * even if the classloader is no longer in use, due to GC delay.
             * this is a poor attempt to see if we can force GC early on.
             */
            for (int i = 0; i < 5; i++) {
                try {
                    System.gc();
                    System.gc();
                    Thread.sleep(1000);

                    System.load(libFile.getPath());
                    return;
                } catch (InterruptedException x) {
                    throw e; // throw the original exception
                } catch (LinkageError x) {
                    // retry
                }
            }
            // still failing after retry.
            throw e;
        }
    }

    private static String md5(File file) throws IOException {
        return null != file && file.exists() ? md5(new FileInputStream(file)) : null;
    }

    private static String md5(InputStream in) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            try {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) >= 0) {
                    md5.update(buf, 0, len);
                }
                return Hex.encode(md5.digest());

            } finally {
                in.close();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private Natives() {
    }
}
