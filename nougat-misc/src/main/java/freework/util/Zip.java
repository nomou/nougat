/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import freework.io.IOUtils;
import freework.io.Path;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * A sample zip compression utils.
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings({"PMD.AbstractClassShouldStartWithAbstractNamingRule"})
public abstract class Zip {

    /**
     * Non-instantiate.
     */
    private Zip() {
    }

    /**
     * Compresses the given files(directories) as archive file.
     *
     * @param files       the source files(directories)
     * @param archivePath the archive file path
     * @param charset     the compress encoding
     * @throws IOException if an I/O error occurs
     */
    public static void compressTo(final File[] files, final File archivePath, final Charset charset) throws IOException {
        final File dir = archivePath.getParentFile();
        createDirectoryIfNecessary(dir);
        compressTo(files, new FileOutputStream(archivePath), charset);
    }

    /**
     * Compresses the given files(directories) to {@link OutputStream}.
     *
     * @param files   the source files(directories)
     * @param out     the output stream
     * @param charset the compress encoding
     * @throws IOException if an I/O error occurs
     */
    public static void compressTo(final File[] files, final OutputStream out, final Charset charset) throws IOException {
        // for jdk 1.7
        final ZipOutputStream zipOut = new ZipOutputStream(out, charset);
        for (final File f : files) {
            compressEntry(f, normalizePath(f.getParentFile().getAbsolutePath()), zipOut);
        }
        zipOut.flush();
        zipOut.close();
    }

    /**
     * Compresses file or directory tree to {@link ZipOutputStream}.
     *
     * @param file    the file or directory
     * @param baseDir the base path for zip entry
     * @param zipOut  the zip output stream
     * @throws IOException if an I/O error occurs
     */
    private static void compressEntry(final File file, final String baseDir, final ZipOutputStream zipOut) throws IOException {
        String relativePath = normalizePath(file.getAbsolutePath()).substring(baseDir.length());
        // zip directory entry must be ends with "/".
        relativePath = file.isDirectory() && !relativePath.endsWith("/") ? relativePath + '/' : relativePath;
        relativePath = relativePath.startsWith("/") || relativePath.startsWith("\\") ? relativePath.substring(1) : relativePath;

        // Add a entry
        zipOut.putNextEntry(new ZipEntry(relativePath));
        if (file.isFile()) {
            IOUtils.flow(new FileInputStream(file), zipOut, true, false);
        }
        zipOut.closeEntry();

        // if not a symbolic link.
        if (file.isDirectory() && !isSymbolicLink(file)) {
            final File[] files = file.listFiles();
            if (null != files) {
                for (final File f : files) {
                    compressEntry(f, baseDir, zipOut);
                }
            }
        }
    }

    /**
     * Extracts the compressed file to a given output directory.
     *
     * @param archiveFile the compressed file
     * @param outputDir   the output directory
     * @param charset     the compressed encoding
     * @throws IOException if an I/O error occurs
     */
    public static void extract(final File archiveFile, final String outputDir, final Charset charset) throws IOException {
        final File outDir = new File(outputDir);
        createDirectoryIfNecessary(outDir);

        // jdk 1.7
        final ZipFile zipFile = new ZipFile(archiveFile, charset);
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            final File entryPath = new File(outDir.getAbsolutePath() + '/' + entry.getName());
            if (entry.isDirectory()) {
                createDirectoryIfNecessary(entryPath);
                continue;
            }

            createDirectoryIfNecessary(entryPath.getParentFile());

            final InputStream in = zipFile.getInputStream(entry);
            final FileOutputStream out = new FileOutputStream(entryPath);
            IOUtils.flow(in, out, true, true);
        }
    }

    /**
     * Normalize then given path.
     *
     * @param path the source path
     * @return normalized path
     */
    private static String normalizePath(final String path) {
        return Path.normalize(path);
    }

    /**
     * Returns whether the given file is a symbolic link file.
     *
     * @param file the file
     * @throws IOException if an I/O error occurs
     */
    private static boolean isSymbolicLink(final File file) throws IOException {
        if (null == file) {
            return false;
        }
        final File finalFile = null == file.getParent() ? file : new File(file.getParentFile().getCanonicalFile(), file.getName());
        return !finalFile.getCanonicalFile().equals(finalFile.getAbsoluteFile());
    }

    /**
     * Creates a directory if it doesn't exist.
     *
     * @param path the directory path
     * @throws IOException if an I/O error occurs
     */
    private static void createDirectoryIfNecessary(final File path) throws IOException {
        if (!path.exists() && !path.mkdirs()) {
            throw new IOException("Can't create directory: " + path);
        } else if (path.exists() && !path.isDirectory()) {
            throw new IOException("Path exists and is not a directory: " + path);
        }
    }
}
