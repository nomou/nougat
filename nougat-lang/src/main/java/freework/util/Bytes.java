/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.nio.charset.Charset;

/**
 * Byte array utils.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class Bytes {
    /**
     * UTF-8 charset.
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Non-instantiate.
     */
    private Bytes() {
    }

    /**
     * Converts given integer value to the represented byte array(length is 4).
     *
     * @param integer integer
     * @return the represented byte array
     */
    public static byte[] toBytes(final int integer) {
        final byte[] bytes = new byte[4];
        bytes[0] = (byte) (0xFF & ((0xFF000000 & integer) >> 24));
        bytes[1] = (byte) (0xFF & ((0x00FF0000 & integer) >> 16));
        bytes[2] = (byte) (0xFF & ((0x0000FF00 & integer) >> 8));
        bytes[3] = (byte) (0xFF & integer);
        return bytes;
    }

    /**
     * Converts given long value to the represented byte array(length is 8).
     *
     * @param l long value
     * @return the represented byte array
     */
    public static byte[] toBytes(final long l) {
        final byte[] bytes = new byte[8];
        bytes[0] = (byte) (0xFF & ((0xFF00000000000000L & l) >> 56));
        bytes[1] = (byte) (0xFF & ((0x00FF000000000000L & l) >> 48));
        bytes[2] = (byte) (0xFF & ((0x0000FF0000000000L & l) >> 40));
        bytes[3] = (byte) (0xFF & ((0x000000FF00000000L & l) >> 32));
        bytes[4] = (byte) (0xFF & ((0x00000000FF000000L & l) >> 24));
        bytes[5] = (byte) (0xFF & ((0x0000000000FF0000L & l) >> 16));
        bytes[6] = (byte) (0xFF & ((0x000000000000FF00L & l) >> 8));
        bytes[7] = (byte) (0xFF & l);
        return bytes;
    }

    /**
     * Converts given char array to the represented byte array.
     *
     * @param chars char array
     * @return the represented byte array
     */
    public static byte[] toBytes(final char[] chars) {
        return toBytes(chars, 0, chars.length);
    }

    /**
     * Converts given char array to the represented byte array.
     *
     * @param chars  char array
     * @param offset char array offset
     * @param len    char array length
     * @return the represented byte array
     */
    public static byte[] toBytes(final char[] chars, final int offset, final int len) {
        return toBytes(String.valueOf(chars, offset, len));
    }

    /**
     * Converts given string to the represented byte array.
     *
     * @param text the string
     * @return the represented byte array
     */
    public static byte[] toBytes(final String text) {
        return toBytes(text, UTF_8);
    }

    /**
     * Converts given string to the represented byte array.
     *
     * @param text    the string
     * @param charset string convert to byte array used charset
     * @return the represented byte array
     */
    public static byte[] toBytes(final String text, final Charset charset) {
        return text.getBytes(charset);
    }

    /* ************************************
     *
     * ********************************** */

    /**
     * Converts byte array to the represented integer value.
     *
     * @param bytes byte array, length must not be greater than 4
     * @return the represented integer
     */
    public static int toInt(final byte[] bytes) {
        if (bytes.length > 4) {
            throw new IllegalArgumentException("bytes length must not be greater than 4");
        }
        int shift = 0;
        int result = 0;
        for (int i = bytes.length - 1; i >= 0; i--, shift += 8) {
            result |= ((bytes[i] & 0xFF) << shift);
        }
        return result;
    }

    /**
     * Converts byte array to the represented long value.
     *
     * @param bytes byte array, length must not be greater than 8
     * @return the represented long
     */
    public static long toLong(final byte[] bytes) {
        if (bytes.length > 8) {
            throw new IllegalArgumentException("bytes length must not be greater than 8");
        }
        int shift = 0;
        long result = 0;
        for (int i = bytes.length - 1; i >= 0; i--, shift += 8) {
            result |= ((long) (bytes[i] & 0xFF) << shift);
        }
        return result;
    }

    /**
     * Converts byte array to the represented string.
     *
     * @param bytes byte array
     * @return the represented string
     */
    public static String toString(final byte[] bytes) {
        return toString(bytes, UTF_8);
    }

    /**
     * Converts byte array to the represented string.
     *
     * @param bytes   byte array
     * @param charset byte array to string used charset
     * @return the represented string
     */
    public static String toString(final byte[] bytes, final Charset charset) {
        return new String(bytes, charset);
    }

    /**
     * Converts byte array to the represented char array.
     *
     * @param bytes byte array
     * @return the represented char array
     */
    public static char[] toChars(final byte[] bytes) {
        return toChars(bytes, UTF_8);
    }

    /**
     * Converts given byte array to the represented char array.
     *
     * @param bytes   byte array
     * @param charset byte array to char array used charset
     * @return the represented char array
     */
    public static char[] toChars(final byte[] bytes, final Charset charset) {
        return toString(bytes, charset).toCharArray();
    }

    /**
     * Converts given target to the represented byte array.
     *
     * @param o target
     * @return the represented byte array
     */
    public static byte[] toBytes(final Object o) {
        if (o == null) {
            final String msg = "Argument for String conversion cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (o instanceof Integer) {
            return toBytes(((Integer) o).intValue());
        } else if (o instanceof Long) {
            return toBytes(((Long) o).longValue());
        } else if (o instanceof byte[]) {
            return (byte[]) o;
        } else if (o instanceof char[]) {
            return toBytes((char[]) o);
        } else if (o instanceof String) {
            return toBytes((String) o);
        } else {
            throw new UnsupportedOperationException("can't convert " + o.getClass() + " to byte[]");
        }
    }
}
