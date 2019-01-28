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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Hash-based Message Authentication Code.
 * <p>
 * Usage: new Hmac.MD5(keyBytes, "message").toHex();
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings({"PMD.ClassNamingShouldBeCamelRule"})
public class Hmac {

    /**
     * HMAC algorithm.
     */
    @SuppressWarnings("PMD.EnumConstantsMustHaveCommentRule")
    public enum Algorithm {
        HMAC_MD5("HmacMD5"), HMAC_SHA1("HmacSHA1"), HMAC_SHA256("HmacSHA256"), HMAC_SHA384("HmacSHA384"), HMAC_SHA512("HmacSHA512");

        private final String name;

        Algorithm(final String algorithm) {
            this.name = algorithm;
        }
    }

    /**
     * HMAC-MD5.
     */
    public static class MD5 extends Hmac {

        public MD5(final String key, final Object source) {
            this(Bytes.toBytes(key), source);
        }

        public MD5(final byte[] key, final Object source) {
            super(Algorithm.HMAC_MD5, key, source);
        }
    }

    /**
     * HMAC-SHA1.
     */
    public static class SHA1 extends Hmac {

        public SHA1(final String key, final Object source) {
            this(Bytes.toBytes(key), source);
        }

        public SHA1(final byte[] key, final Object source) {
            super(Algorithm.HMAC_SHA1, key, source);
        }
    }

    /**
     * HMAC-SHA256.
     */
    public static class SHA256 extends Hmac {

        public SHA256(final String key, final Object source) {
            this(Bytes.toBytes(key), source);
        }

        public SHA256(final byte[] key, final Object source) {
            super(Algorithm.HMAC_SHA256, key, source);
        }
    }

    /**
     * HMAC-SHA384.
     */
    public static class SHA384 extends Hmac {

        public SHA384(final String key, final Object source) {
            this(Bytes.toBytes(key), source);
        }

        public SHA384(final byte[] key, final Object source) {
            super(Algorithm.HMAC_SHA384, key, source);
        }
    }

    /**
     * HMAC-SHA512.
     */
    public static class SHA512 extends Hmac {

        public SHA512(final String key, final Object source) {
            this(Bytes.toBytes(key), source);
        }

        public SHA512(final byte[] key, final Object source) {
            super(Algorithm.HMAC_SHA512, key, source);
        }
    }


    /* ********************************
     *
     * ********************************/

    private byte[] bytes;
    private String hexEncoded;
    private String base64Encoded;

    /**
     * Creates HMAC instance.
     *
     * @param algorithm the name of hash algorithm
     * @param key       the HMAC key
     * @param source    the hash source
     */
    public Hmac(final Algorithm algorithm, final String key, final Object source) {
        this(algorithm, Bytes.toBytes(key), source);
    }

    /**
     * Creates HMAC instance.
     *
     * @param algorithm the name of hash algorithm
     * @param key       the HMAC key
     * @param source    the hash source
     */
    public Hmac(final Algorithm algorithm, final byte[] key, final Object source) {
        if (algorithm == null || key == null || source == null) {
            throw new NullPointerException("algorithm, key and source argument cannot be null.");
        }

        this.hash(algorithm, key, Bytes.toBytes(source));
    }

    /**
     * Performing the hash digest authentication code computation.
     *
     * @param algorithm the name of hash algorithm
     * @param key       the HMAC key
     * @param source    the hash source
     */
    private void hash(final Algorithm algorithm, final byte[] key, final byte[] source) {
        try {
            bytes = hash(algorithm.name, key, source);
        } catch (GeneralSecurityException e) {
            bytes = Throwables.unchecked(e);
        }
    }

    /**
     * Performing the HMAC computation.
     *
     * @param algorithm the name of hash algorithm
     * @param key       the HMAC key
     * @param bytes     the hash source
     * @return the HMAC
     */
    private byte[] hash(final String algorithm, final byte[] key, final byte[] bytes)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        return instantiateMac(algorithm, key).doFinal(bytes);
    }

    /**
     * Instantiate and initialize Mac.
     *
     * @param algorithm the name of HMAC
     * @param key       the key
     * @return mac instance
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private Mac instantiateMac(final String algorithm, final byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
        final SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm);
        final Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);
        mac.reset();
        return mac;
    }

    /**
     * Gets the array of bytes for the resulting HMAC.
     *
     * @return HMAC
     */
    public byte[] getBytes() {
        return bytes.clone();
    }

    /**
     * Gets the Hexadecimal for the resulting HMAC.
     *
     * @return HMAC
     */
    public String toHex() {
        if (null == hexEncoded) {
            hexEncoded = Hex.encode(bytes);
        }
        return hexEncoded;
    }

    /**
     * Gets the base64 for the resulting HMAC.
     *
     * @return HMAC
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
}
