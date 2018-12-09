/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.codec;

/**
 * Encoding and decoding for Hexadecimal.
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings({"PMD.AbstractClassShouldStartWithAbstractNamingRule"})
public abstract class Hex {
    /**
     * Hexadecimal char array.
     */
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Hexadecimal bits shift.
     */
    private static final int SHIFT = 4;

    /**
     * Hexadecimal mask.
     */
    private static final int MASK = (1 << SHIFT) - 1;

    /**
     * Non-instantiate.
     */
    private Hex() {
    }

    /**
     * Encodes the byte array to String.
     *
     * @param bytes the byte array to Hex-encode
     * @return a String representation of the resultant hex-encode
     */
    public static String encode(final byte[] bytes) {
        return encode(bytes, 0, bytes.length);
    }

    /**
     * Encodes the byte array to String.
     *
     * @param bytes  the byte array to hex-encode
     * @param offset the byte array offset
     * @param length the byte array length
     * @return a String representation of the resultant hex-encode
     */
    public static String encode(final byte[] bytes, final int offset, final int length) {
        final StringBuilder hex = new StringBuilder(length << 1);

        for (int i = offset; i < offset + length; i++) {
            final byte b = bytes[i];
            hex.append(DIGITS[(b >>> SHIFT) & MASK]).append(DIGITS[b & MASK]);
        }

        return hex.toString();
    }

    /**
     * Decodes the hex-encoded string to byte array.
     *
     * @param hex a hex-encoded string
     * @return decoded byte array
     */
    public static byte[] decode(final String hex) {
        return decode(hex.toCharArray());
    }

    /**
     * Decodes the hex-encoded char array to byte array.
     *
     * @param hex a hex-encoded char array
     * @return decoded byte array
     */
    public static byte[] decode(final char[] hex) {
        final int len = hex.length;
        if (0 != (len & 1)) {
            throw new IllegalArgumentException("Hex must be exactly two digits per byte.");
        }
        final byte[] bytes = new byte[len >> 1];
        for (int i = 0, j = 0; i < bytes.length; i++) {
            final int h = toDigit(hex[j++], j - 1) << 4;
            final int l = toDigit(hex[j++], j - 1);
            bytes[i] = (byte) ((h | l) & 0xFF);
        }
        return bytes;
    }

    /**
     * Converts a hexadecimal character to an integer.
     *
     * @param ch    A character to convert to an integer digit
     * @param index The index of the character in the source
     * @return an integer
     */
    private static int toDigit(final char ch, final int index) {
        final int digit = Character.digit(ch, 16);
        if (0 > digit) {
            throw new IllegalArgumentException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }
}

