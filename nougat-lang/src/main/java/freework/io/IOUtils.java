/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

/**
 * I/O stream utils.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class IOUtils {
    public static final int END_OF_INPUT = -1;
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 8192;
    private static final int DEFAULT_CHAR_BUFFER_SIZE = 100;

    /**
     * Non-instantiate.
     */
    private IOUtils() {
    }

    /**
     * Reads the contents of an input stream and write to a output stream.
     *
     * @param in  the input stream
     * @param out the output stream
     * @return the number of bytes read
     * @throws IOException if an I/O error occurs
     */
    public static long flow(final InputStream in, final OutputStream out,
                            final boolean closeIn, final boolean closeOut) throws IOException {
        return flow(in, out, new byte[DEFAULT_BYTE_BUFFER_SIZE], closeIn, closeOut);
    }

    /**
     * Reads the contents of an input stream and write to a output stream.
     *
     * @param in       the input stream
     * @param out      the output stream
     * @param buffer   the buffer to use for the read/write
     * @param closeIn  close the input stream
     * @param closeOut close the output stream
     * @return the number of bytes read
     * @throws IOException if an I/O error occurs
     */
    public static long flow(final InputStream in, final OutputStream out, final byte[] buffer,
                            final boolean closeIn, final boolean closeOut) throws IOException {
        long read = 0;
        try {
            int len;
            while (END_OF_INPUT < (len = in.read(buffer))) {
                out.write(buffer, 0, len);
                read += len;
            }
            out.flush();
            return read;
        } finally {
            if (closeIn) {
                close(in);
            }
            if (closeOut) {
                close(out);
            }
        }
    }

    /**
     * Reads the contents of a Reader and write to a Writer.
     *
     * @param reader   the reader
     * @param writer   the writer
     * @param closeIn  close the reader
     * @param closeOut close the writer
     * @return the number of characters read
     * @throws IOException if an I/O error occurs
     */
    public static long flow(final Reader reader, final Writer writer,
                            final boolean closeIn, final boolean closeOut) throws IOException {
        return flow(reader, writer, new char[DEFAULT_CHAR_BUFFER_SIZE], closeIn, closeOut);
    }

    /**
     * Reads the contents of a Reader and write to a Writer.
     *
     * @param reader   the reader
     * @param writer   the writer
     * @param buffer   the buffer to use for the read/write
     * @param closeIn  close the reader
     * @param closeOut close the writer
     * @return the number of characters read
     * @throws IOException if an I/O error occurs
     */
    public static long flow(final Reader reader, final Writer writer, final char[] buffer,
                            final boolean closeIn, final boolean closeOut) throws IOException {
        long read = 0;
        try {
            int len;
            while (END_OF_INPUT < (len = reader.read(buffer))) {
                writer.write(buffer, 0, len);
                read += len;
            }
            writer.flush();
            return read;
        } finally {
            if (closeIn) {
                close(reader);
            }
            if (closeOut) {
                close(writer);
            }
        }
    }

    /**
     * Reads the contents of a {@link ReadableByteChannel} and write to a {@link WritableByteChannel}.
     *
     * @param rbc      the read channel
     * @param wbc      the write channel
     * @param closeIn  close the read channel
     * @param closeOut close the write channel
     * @return the number of bytes read
     * @throws IOException if an I/O error occurs
     */
    public static long flow(final ReadableByteChannel rbc, final WritableByteChannel wbc,
                            final boolean closeIn, final boolean closeOut) throws IOException {
        return flow(rbc, wbc, ByteBuffer.allocate(DEFAULT_BYTE_BUFFER_SIZE), closeIn, closeOut);
    }

    /**
     * Reads the contents of a {@link ReadableByteChannel} and write to a {@link WritableByteChannel}.
     *
     * @param rbc      the read channel
     * @param wbc      the write channel
     * @param closeIn  close the read channel
     * @param closeOut close the write channel
     * @return the number of bytes read
     * @throws IOException if an I/O error occurs
     */
    public static long flow(final ReadableByteChannel rbc, final WritableByteChannel wbc, final ByteBuffer buffer,
                            final boolean closeIn, final boolean closeOut) throws IOException {
        long read = 0;
        try {
            while (END_OF_INPUT < rbc.read(buffer)) {
                read += buffer.limit();
                buffer.flip();
                wbc.write(buffer);
                buffer.clear();
            }
            return read;
        } finally {
            if (closeIn) {
                close(rbc);
            }
            if (closeOut) {
                close(wbc);
            }
        }
    }

    /**
     * Reads the requested number of bytes or fail if there are not enough left.
     *
     * @param in      the input stream
     * @param buffer  the destination
     * @param offset  initial offset into buffer
     * @param closeIn close the input stream
     * @throws IOException
     */
    public static void readFully(final InputStream in, final byte[] buffer,
                                 final int offset, final boolean closeIn) throws IOException {
        int length = buffer.length - offset;
        int actual = read(in, buffer, offset, length, closeIn);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

    /**
     * Reads bytes from input stream.
     *
     * @param in      the input stream
     * @param buffer  the destination
     * @param offset  initial offset into buffer
     * @param length  expected length of bytes read
     * @param closeIn close the input stream
     * @return actual length of bytes read
     * @throws IOException
     */
    public static int read(final InputStream in, final byte[] buffer,
                           final int offset, final int length, boolean closeIn) throws IOException {
        if (0 > length) {
            throw new IllegalArgumentException("length must be greater than or equal 0");
        }

        int remaining = length;
        while (remaining > 0) {
            int location = length - remaining;
            int len = in.read(buffer, offset + location, remaining);
            if (END_OF_INPUT == len) {
                break;
            }
            remaining -= len;
        }

        if (closeIn) {
            close(in);
        }

        return length - remaining;
    }

    /**
     * Returns the given input stream if it is a {@link BufferedInputStream}, otherwise creates a {@link BufferedInputStream}.
     *
     * @param in the input stream
     * @return the given input stream or a new {@link BufferedInputStream}
     */
    public static BufferedInputStream buffer(final InputStream in) {
        return null == in || in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in);
    }

    /**
     * Returns the given output stream if it is a {@link BufferedOutputStream}, otherwise creates a {@link BufferedOutputStream}.
     *
     * @param out the output stream
     * @return the given output stream or a new {@link BufferedOutputStream}
     */
    public static BufferedOutputStream buffer(final OutputStream out) {
        return null == out || out instanceof BufferedOutputStream ? (BufferedOutputStream) out : new BufferedOutputStream(out);
    }

    /**
     * Returns the given reader if it is a {@link BufferedReader}, otherwise creates a {@link BufferedReader}.
     *
     * @param reader the reader
     * @return the given reader or a new {@link BufferedReader}
     */
    public static BufferedReader buffer(final Reader reader) {
        return null == reader || reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * Returns the given reader if it is a {@link BufferedWriter}, otherwise creates a {@link BufferedWriter}.
     *
     * @param writer the writer
     * @return the given writer or a new {@link BufferedWriter}
     */
    public static BufferedWriter buffer(final Writer writer) {
        return null == writer || writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    /**
     * Gets the contents of an input stream as byte[].
     *
     * @param in the input stream
     * @return the array of byte
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(final InputStream in) throws IOException {
        final int len = in.available();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1 > len ? 1024 : len);

        flow(in, baos, true, true);
        return baos.toByteArray();
    }

    /**
     * Gets the contents of an input stream as a String using the specified character encoding.
     *
     * @param in      the input stream
     * @param charset the encoding to use
     * @param close   close the input stream
     * @return the contents
     * @throws IOException if an I/O error occurs
     */
    public static String toString(final InputStream in, final Charset charset, final boolean close) throws IOException {
        final Reader reader = charset == null ? new InputStreamReader(in) : new InputStreamReader(in, charset);
        return toString(reader, close);
    }

    /**
     * Gets the contents of an Reader as a String.
     *
     * @param reader the reader
     * @param close  close the reader
     * @return the contents
     * @throws IOException if an I/O error occurs
     */
    public static String toString(final Reader reader, final boolean close) throws IOException {
        final StringWriter writer = new StringWriter();
        flow(reader, writer, close, true);
        return writer.toString();
    }

    /**
     * Closes the {@link Closeable} object.
     *
     * @param closeable the closeable object
     */
    public static void close(final Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (final IOException ignore) {
                // quietly
            }
        }
    }
}
