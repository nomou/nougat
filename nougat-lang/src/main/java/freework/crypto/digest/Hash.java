/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.crypto.digest;

import freework.codec.Base64;
import freework.codec.Hex;
import freework.util.Bytes;
import freework.util.Throwables;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Message-Digest.
 * <p>
 * Usage:
 * <ul>
 * <li>new Hash.MD5("foo").toHex()</li>
 * <li>DigestInputStream hashStream = Hash.wrap(Algorithm.MD5, in)</li>
 * </ul>
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings({"PMD.ClassNamingShouldBeCamelRule"})
public class Hash {

    /**
     * Hash algorithm.
     */
    @SuppressWarnings("PMD.EnumConstantsMustHaveCommentRule")
    public enum Algorithm {
        MD2("MD2"), MD5("MD5"), SHA1("SHA-1"), SHA256("SHA-256"), SHA384("SHA-384"), SHA512("SHA-512");

        final String name;

        /**
         * Hash algorithm.
         *
         * @param algorithm the name of algorithm
         */
        Algorithm(final String algorithm) {
            this.name = algorithm;
        }
    }

    /**
     * MD5 Hash.
     */
    public static class MD5 extends Hash {
        public MD5(final Object source) {
            this(source, null);
        }

        public MD5(final Object source, final Object salt) {
            this(source, salt, DEFAULT_ITERATIONS);
        }

        public MD5(final Object source, final Object salt, final int hashIterations) {
            super(Algorithm.MD5, source, salt, hashIterations);
        }
    }

    /**
     * SHA-1 Hash.
     */
    public static class SHA1 extends Hash {
        public SHA1(final Object source) {
            this(source, null);
        }

        public SHA1(final Object source, final Object salt) {
            this(source, salt, DEFAULT_ITERATIONS);
        }

        public SHA1(final Object source, final Object salt, final int hashIterations) {
            super(Algorithm.SHA1, source, salt, hashIterations);
        }
    }

    /**
     * SHA-256.
     */
    public static class SHA256 extends Hash {

        public SHA256(final Object source) {
            this(source, null);
        }

        public SHA256(final Object source, final Object salt) {
            this(source, salt, DEFAULT_ITERATIONS);
        }

        public SHA256(final Object source, final Object salt, final int hashIterations) {
            super(Algorithm.SHA256, source, salt, hashIterations);
        }
    }

    /**
     * SHA-512.
     */
    public static class SHA512 extends Hash {

        public SHA512(final Object source) {
            this(source, null);
        }

        public SHA512(final Object source, final Object salt) {
            this(source, salt, DEFAULT_ITERATIONS);
        }

        public SHA512(final Object source, final Object salt, final int hashIterations) {
            super(Algorithm.SHA512, source, salt, hashIterations);
        }

    }

    /* ********************************
     *
     * ********************************/

    protected static final int DEFAULT_ITERATIONS = 1;

    private byte[] bytes;
    private String hexEncoded;
    private String base64Encoded;

    /**
     * Creates Hash instance.
     *
     * @param algorithm the name of hash algorithm
     * @param source    the hash source
     */
    public Hash(final Algorithm algorithm, final Object source) {
        this(algorithm, source, null);
    }

    /**
     * Creates Hash instance.
     *
     * @param algorithm the name of hash algorithm
     * @param source    the hash source
     * @param salt      the hash salt
     */
    public Hash(final Algorithm algorithm, final Object source, final Object salt) {
        this(algorithm, source, salt, DEFAULT_ITERATIONS);
    }

    /**
     * Creates Hash instance.
     *
     * @param algorithm      the name of hash algorithm
     * @param source         the hash source
     * @param salt           the hash salt
     * @param hashIterations hash iterations
     */
    public Hash(final Algorithm algorithm, final Object source, final Object salt, final int hashIterations) {
        if (algorithm == null || source == null) {
            throw new NullPointerException("algorithm and source argument cannot be null.");
        }

        final byte[] sourceBytes = Bytes.toBytes(source);
        final byte[] saltBytes = salt == null ? null : Bytes.toBytes(salt);
        final int finalHashIterations = Math.max(hashIterations, DEFAULT_ITERATIONS);

        this.hash(algorithm, sourceBytes, saltBytes, finalHashIterations);
    }

    /**
     * Performing the hash computation.
     *
     * @param algorithm  the name of hash algorithm
     * @param source     the hash source
     * @param salt       the hash salt
     * @param iterations hash iterations
     */
    private void hash(final Algorithm algorithm, final byte[] source, final byte[] salt, final int iterations) {
        try {
            bytes = hash(algorithm.name, source, salt, iterations);
        } catch (final NoSuchAlgorithmException e) {
            bytes = Throwables.unchecked(e);
        }
    }

    /**
     * Performing the hash computation.
     *
     * @param algorithm      the name of hash algorithm
     * @param bytes          the hash source
     * @param salt           the hash salt
     * @param hashIterations hash iterations
     * @return the array of bytes for the resulting hash value.
     */
    private byte[] hash(final String algorithm, final byte[] bytes, final byte[] salt, final int hashIterations)
            throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance(algorithm);

        if (null != salt) {
            digest.reset();
            digest.update(salt);
        }
        byte[] hashed = digest.digest(bytes);

        for (int i = 0; i < hashIterations - 1; i++) {
            digest.reset();
            hashed = digest.digest(hashed);
        }

        return hashed;
    }

    /**
     * Gets the array of bytes for the resulting hash value.
     *
     * @return hash value
     */
    public byte[] getBytes() {
        return bytes.clone();
    }

    /**
     * Gets the Hexadecimal for the resulting hash value.
     *
     * @return hash value
     */
    public String toHex() {
        if (null == hexEncoded) {
            hexEncoded = Hex.encode(bytes);
        }
        return hexEncoded;
    }

    /**
     * Gets the base64 for the resulting hash value.
     *
     * @return hash value
     */
    public String toBase64() {
        if (null == base64Encoded) {
            base64Encoded = Base64.encodeToString(bytes);
        }
        return base64Encoded;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toHex();
    }

    /**
     * Creates an output stream using given message digest algorithm.
     *
     * @param algorithm the name of algorithm
     * @return the message digest output stream
     */
    public static DigestOutputStream stream(final Algorithm algorithm) {
        return wrap(algorithm, (OutputStream) null);
    }

    /**
     * Wraps an input stream using given message digest algorithm.
     *
     * @param algorithm the name of algorithm
     * @param in        the input stream
     * @return the message digest wrapped input stream
     */
    public static DigestInputStream wrap(final Algorithm algorithm, final InputStream in) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(algorithm.name);
            return new DigestInputStream(in, digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Wraps an output stream using given message digest algorithm.
     *
     * @param algorithm the name of algorithm
     * @param out       the output stream
     * @return the message digest wrapped output stream
     */
    public static DigestOutputStream wrap(final Algorithm algorithm, final OutputStream out) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(algorithm.name);
            return new DigestOutputStream(null != out ? out : NullOutputStream.INSTANCE, digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Null output stream.
     */
    private static class NullOutputStream extends OutputStream {
        private static final NullOutputStream INSTANCE = new NullOutputStream();

        @Override
        public void write(int b) throws IOException {
            // to /dev/null
        }
    }
}
