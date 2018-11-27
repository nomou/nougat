package freework.crypto.cipher;

import freework.codec.Base64;
import freework.util.Bytes;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Java encryption and decryption.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class Crypt {

    /**
     * Non-instantiate.
     */
    private Crypt() {
    }

    /**
     * Encrypt the plain text and use 'Base64' format.
     *
     * @param plain plain text
     * @return cipher text
     */
    public String encrypt(final String plain) {
        return encrypt(plain, null);
    }

    /**
     * Parse and decrypt the 'Base64' formatted cipher text.
     *
     * @param cipher cipher text
     * @return plain text
     */
    public String decrypt(final String cipher) {
        return decrypt(cipher, null);
    }

    /**
     * Encrypt plain text and format cipher text.
     *
     * @param plain  plain text
     * @param format cipher format
     * @return formatted cipher text
     */
    public String encrypt(final String plain, final Format format) {
        final Format fmt = null != format ? format : Format.BASE64;
        return null != plain ? fmt.format(encrypt(Bytes.toBytes(plain))) : null;
    }

    /**
     * Parse formatted cipher text and decrypt it.
     *
     * @param cipher formatted cipher text
     * @param format cipher format
     * @return plain text
     */
    public String decrypt(final String cipher, final Format format) {
        final Format fmt = null != format ? format : Format.BASE64;
        return null != cipher ? Bytes.toString(decrypt(fmt.parse(cipher))) : null;
    }

    /**
     * Encrypt given plain bytes.
     *
     * @param bytes plain bytes
     * @return cipher bytes
     */
    public abstract byte[] encrypt(final byte[] bytes);

    /**
     * Decrypt given cipher bytes.
     *
     * @param bytes cipher bytes
     * @return plain bytes
     */
    public abstract byte[] decrypt(final byte[] bytes);

    /**
     * Wraps an input stream use decrypt mode.
     *
     * @param in the input stream
     * @return decryption input stream
     */
    public InputStream wrap(final InputStream in) {
        return wrap(Cipher.DECRYPT_MODE, in);
    }

    /**
     * Wraps an output stream use encrypt mode.
     *
     * @param out the output stream
     * @return encryption output stream
     */
    public OutputStream wrap(final OutputStream out) {
        return wrap(Cipher.ENCRYPT_MODE, out);
    }

    /**
     * Wraps an input stream.
     *
     * @param opmode the operation mode of this cipher
     *               (this is one of the following: Cipher#ENCRYPT_MODE, Cipher#DECRYPT_MODE)
     * @param in     the input stream
     * @return encryption or decryption input stream
     */
    public abstract InputStream wrap(final int opmode, final InputStream in);

    /**
     * Wraps an output stream.
     *
     * @param opmode the operation mode of this cipher
     *               (this is one of the following: Cipher#ENCRYPT_MODE, Cipher#DECRYPT_MODE)
     * @param out    the output stream
     * @return encryption or decryption output stream
     */
    public abstract OutputStream wrap(final int opmode, final OutputStream out);

    /**
     * Encrypt/decrypt given bytes or wrap given input/output stream.
     *
     * @param transformation the name of the transformation
     * @param opmode         the operation mode of this cipher
     *                       (this is one of the following: Cipher#ENCRYPT_MODE, Cipher#DECRYPT_MODE)
     * @param key            the key of encryption or decryption
     * @param params         the algorithm parameters
     * @param source         bytes or input/output stream
     * @param <T>            source type
     * @return cipher/plain bytes or wrapped input/output stream
     */
    @SuppressWarnings({"unchecked"})
    protected <T> T doCrypt(final String transformation, final int opmode, final Key key, final AlgorithmParameterSpec params, final T source) {
        try {
            if (source instanceof byte[]) {
                return (T) this.doCryptInternal(transformation, opmode, key, params, new SecureRandom(), (byte[]) source);
            } else if (source instanceof InputStream) {
                return (T) this.doCryptInternal(transformation, opmode, key, params, new SecureRandom(), (InputStream) source);
            } else if (source instanceof OutputStream) {
                return (T) this.doCryptInternal(transformation, opmode, key, params, new SecureRandom(), (OutputStream) source);
            } else {
                throw new IllegalArgumentException("source must be instance of byte[] or InputStream or OutputStream");
            }
        } catch (final NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (final InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        } catch (final InvalidKeyException e) {
            throw new IllegalStateException(e);
        } catch (final IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        } catch (final BadPaddingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Encrypt or decrypt given bytes.
     *
     * @param transformation the name of the transformation
     * @param opmode         the operation mode of this cipher
     * @param key            the key of encryption or decryption
     * @param params         the algorithm parameters
     * @param random         the source of randomness
     * @param bytes          plain/cipher bytes
     * @return cipher/plain bytes
     * @throws NoSuchPaddingException             if transformation contains a padding scheme that is not available.
     * @throws NoSuchAlgorithmException           if transformation is null, empty, in an invalid format, or if no
     *                                            Provider supports a CipherSpi implementation for the specified algorithm.
     * @throws InvalidAlgorithmParameterException if the given algorithm parameters are inappropriate for this cipher,
     *                                            or this cipher requires algorithm parameters and params is null,
     *                                            or the given algorithm parameters imply a cryptographic strength
     *                                            that would exceed the legal limits (as determined from the configured
     * @throws InvalidKeyException                if the given key is inappropriate for initializing this cipher,
     *                                            or its keysize exceeds the maximum allowable keysize
     *                                            (as determined from the configured jurisdiction policy files).
     * @throws IllegalBlockSizeException          if this cipher is a block cipher, no padding has been requested
     *                                            (only in encryption mode), and the total input length of the data
     *                                            processed by this cipher is not a multiple of block size;
     *                                            or if this encryption algorithm is unable to process the input data provided.
     * @throws BadPaddingException                if this cipher is in decryption mode, and (un)padding has been requested,
     *                                            but the decrypted data is not bounded by the appropriate padding bytes
     */
    private byte[] doCryptInternal(final String transformation, final int opmode, final Key key,
                                   final AlgorithmParameterSpec params, final SecureRandom random, final byte[] bytes)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return this.instantiateCipher(transformation, opmode, key, params, random).doFinal(bytes);
    }

    /**
     * Create a cipher and wrap an output stream.
     *
     * @param transformation the name of the transformation
     * @param opmode         the operation mode of this cipher
     * @param key            the key of encryption or decryption
     * @param params         the algorithm parameters
     * @param random         the source of randomness
     * @param out            the output stream
     * @return encryption or decryption output stream
     * @throws NoSuchPaddingException             if transformation contains a padding scheme that is not available.
     * @throws NoSuchAlgorithmException           if transformation is null, empty, in an invalid format, or if no
     *                                            Provider supports a CipherSpi implementation for the specified algorithm.
     * @throws InvalidAlgorithmParameterException if the given algorithm parameters are inappropriate for this cipher,
     *                                            or this cipher requires algorithm parameters and params is null,
     *                                            or the given algorithm parameters imply a cryptographic strength
     *                                            that would exceed the legal limits (as determined from the configured
     *                                            jurisdiction policy files).
     * @throws InvalidKeyException                if the given key is inappropriate for initializing this cipher,
     *                                            or its keysize exceeds the maximum allowable keysize
     *                                            (as determined from the configured jurisdiction policy files).
     */
    private CipherOutputStream doCryptInternal(final String transformation, final int opmode, final Key key,
                                               final AlgorithmParameterSpec params, final SecureRandom random,
                                               final OutputStream out) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        return new CipherOutputStream(out, this.instantiateCipher(transformation, opmode, key, params, random));
    }

    /**
     * Create a cipher and wrap an input stream.
     *
     * @param transformation the name of the transformation
     * @param opmode         the operation mode of this cipher
     * @param key            the key of encryption or decryption
     * @param params         the algorithm parameters
     * @param random         the source of randomness
     * @param in             the input stream
     * @return encryption or decryption input stream
     * @throws NoSuchPaddingException             if transformation contains a padding scheme that is not available.
     * @throws NoSuchAlgorithmException           if transformation is null, empty, in an invalid format, or if no
     *                                            Provider supports a CipherSpi implementation for the specified algorithm.
     * @throws InvalidAlgorithmParameterException if the given algorithm parameters are inappropriate for this cipher,
     *                                            or this cipher requires algorithm parameters and params is null,
     *                                            or the given algorithm parameters imply a cryptographic strength
     *                                            that would exceed the legal limits (as determined from the configured
     *                                            jurisdiction policy files).
     * @throws InvalidKeyException                if the given key is inappropriate for initializing this cipher,
     *                                            or its keysize exceeds the maximum allowable keysize
     *                                            (as determined from the configured jurisdiction policy files).
     */
    private CipherInputStream doCryptInternal(final String transformation, final int opmode, final Key key,
                                              final AlgorithmParameterSpec params, final SecureRandom random,
                                              final InputStream in) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        return new CipherInputStream(in, this.instantiateCipher(transformation, opmode, key, params, random));
    }

    /**
     * Create a cipher object that implements the specified transformation.
     *
     * @param transformation the name of the transformation
     * @param opmode         the operation mode of this cipher
     * @param key            the key of encryption or decryption
     * @param params         the algorithm parameters
     * @param random         the source of randomness
     * @return a cipher that implements the requested transformation.
     * @throws NoSuchPaddingException             if transformation contains a padding scheme that is not available.
     * @throws NoSuchAlgorithmException           if transformation is null, empty, in an invalid format, or if no
     *                                            Provider supports a CipherSpi implementation for the specified algorithm.
     * @throws InvalidAlgorithmParameterException if the given algorithm parameters are inappropriate for this cipher,
     *                                            or this cipher requires algorithm parameters and params is null,
     *                                            or the given algorithm parameters imply a cryptographic strength
     *                                            that would exceed the legal limits (as determined from the configured
     *                                            jurisdiction policy files).
     * @throws InvalidKeyException                if the given key is inappropriate for initializing this cipher,
     *                                            or its keysize exceeds the maximum allowable keysize
     *                                            (as determined from the configured jurisdiction policy files).
     */
    private Cipher instantiateCipher(final String transformation, final int opmode, final Key key,
                                     final AlgorithmParameterSpec params, final SecureRandom random) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        final Cipher cipher = Cipher.getInstance(transformation);
        if (null == params) {
            if (null == random) {
                cipher.init(opmode, key);
            } else {
                cipher.init(opmode, key, random);
            }
        } else {
            if (null == random) {
                cipher.init(opmode, key, params);
            } else {
                cipher.init(opmode, key, params, random);
            }
        }
        return cipher;
    }

    /* ********************************************
     *
     * ****************************************** */

    /**
     * Symmetric encryption algorithm.
     */
    private static class Symmetric extends Crypt {
        private final String transformation;
        private final SecretKey key;

        /**
         * Instantiate symmetric encryption algorithm crypt.
         *
         * @param transformation the name of the transformation
         * @param key            encrypt/decrypt key
         */
        private Symmetric(final String transformation, final SecretKey key) {
            final String algorithm = key.getAlgorithm();

            if (!transformation.toUpperCase().startsWith(algorithm)) {
                throw new IllegalArgumentException("key algorithm and transformation algorithm not matches");
            }
            this.transformation = transformation;
            this.key = key;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] encrypt(final byte[] bytes) {
            return doCrypt(transformation, Cipher.ENCRYPT_MODE, key, null, bytes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] decrypt(final byte[] bytes) {
            return doCrypt(transformation, Cipher.DECRYPT_MODE, key, null, bytes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream wrap(final int opmode, final InputStream in) {
            return doCrypt(transformation, opmode, key, null, in);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public OutputStream wrap(final int opmode, final OutputStream out) {
            return doCrypt(transformation, opmode, key, null, out);
        }
    }

    /**
     * Asymmetric encryption algorithm.
     */
    private static class Asymmetric extends Crypt {
        private final String transformation;
        private final KeyPair key;

        /**
         * Instantiate asymmetric encryption algorithm crypt.
         *
         * @param transformation the name of the transformation
         * @param key            encrypt and decrypt key pair
         */
        private Asymmetric(final String transformation, final KeyPair key) {
            final PublicKey publicKey = key.getPublic();
            final PrivateKey privateKey = key.getPrivate();
            final String publicAlgorithm = null != publicKey ? publicKey.getAlgorithm() : null;
            final String privateAlgorithm = null != privateKey ? privateKey.getAlgorithm() : null;

            if (null == publicAlgorithm && null == privateAlgorithm) {
                throw new IllegalArgumentException("key pair does not contain any key");
            }
            if (null != publicAlgorithm && null != privateAlgorithm && !publicAlgorithm.equals(privateAlgorithm)) {
                throw new IllegalArgumentException("public key and private key algorithm not matches");
            }

            final String algorithm = null != publicAlgorithm ? publicAlgorithm : privateAlgorithm;
            if (null != transformation && !transformation.toUpperCase().startsWith(algorithm)) {
                throw new IllegalArgumentException("key algorithm and transformation algorithm not matches");
            }

            this.transformation = null != transformation ? transformation : algorithm;
            this.key = key;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] encrypt(final byte[] bytes) {
            return doCrypt(transformation, Cipher.ENCRYPT_MODE, this.getRequiredKey(Cipher.ENCRYPT_MODE), null, bytes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] decrypt(final byte[] bytes) {
            return doCrypt(transformation, Cipher.DECRYPT_MODE, getRequiredKey(Cipher.DECRYPT_MODE), null, bytes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream wrap(final int opmode, final InputStream in) {
            return doCrypt(transformation, opmode, this.getRequiredKey(opmode), null, in);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public OutputStream wrap(final int opmode, final OutputStream out) {
            return doCrypt(transformation, opmode, this.getRequiredKey(opmode), null, out);
        }

        /**
         * Gets operation mode using private / public key.
         *
         * @param opmode the operation mode
         * @return the encrypt/decrypt key
         */
        private Key getRequiredKey(final int opmode) {
            if (Cipher.ENCRYPT_MODE == opmode) {
                final PrivateKey privateKey = key.getPrivate();
                if (null == privateKey) {
                    throw new IllegalStateException("private key is not configure");
                }
                return privateKey;
            } else if (Cipher.DECRYPT_MODE == opmode) {
                final PublicKey publicKey = key.getPublic();
                if (null == publicKey) {
                    throw new IllegalStateException("public key is not configure");
                }
                return publicKey;
            }
            throw new IllegalArgumentException("illegal operation mode: " + opmode);
        }
    }

    /* ********************************************
     *
     * ****************************************** */

    /**
     * Creates a symmetric crypt.
     *
     * @param key the secret key
     * @return the symmetric crypt object
     */
    public static Crypt getSymmetric(final SecretKey key) {
        return getSymmetric(null, key);
    }

    /**
     * Creates a symmetric crypt.
     *
     * @param transformation the name of transformation
     * @param bytes          the encoded secret key
     * @return the symmetric crypt object
     */
    public static Crypt getSymmetric(final String transformation, final byte[] bytes) {
        return getSymmetric(transformation, newSymmetricKey(transformation, bytes));
    }

    /**
     * Creates a symmetric crypt.
     *
     * @param transformation the name of transformation
     * @param key            the secret key
     * @return the symmetric crypt object
     */
    public static Crypt getSymmetric(final String transformation, final SecretKey key) {
        final String finalTransformation = null != transformation ? transformation : key.getAlgorithm();
        return new Symmetric(finalTransformation, key);
    }

    /**
     * Creates a asymmetric crypt.
     *
     * @param key the key pair
     * @return the asymmetric crypt object
     */
    public static Crypt getAsymmetric(final KeyPair key) {
        return getAsymmetric(null, key);
    }

    /**
     * Creates a asymmetric crypt.
     *
     * @param transformation   the name of transformation
     * @param base64PublicKey  the base64 encoded public key
     * @param base64PrivateKey the base64 encoded private key
     * @return the asymmetric crypt object
     */
    public static Crypt getAsymmetric(final String transformation, final String base64PublicKey, final String base64PrivateKey) {
        return getAsymmetric(transformation, newAsymmetricKey(transformation, base64PublicKey, base64PrivateKey));
    }

    /**
     * Creates a asymmetric crypt.
     *
     * @param transformation    the name of transformation
     * @param encodedPublicKey  the encoded public key
     * @param encodedPrivateKey the encoded private key
     * @return the asymmetric crypt object
     */
    public static Crypt getAsymmetric(final String transformation, final byte[] encodedPublicKey, final byte[] encodedPrivateKey) {
        return getAsymmetric(transformation, newAsymmetricKey(transformation, encodedPublicKey, encodedPrivateKey));
    }

    /**
     * Creates a asymmetric crypt.
     *
     * @param transformation the name of transformation
     * @param key            the key pair
     * @return the asymmetric crypt object
     */
    public static Crypt getAsymmetric(final String transformation, final KeyPair key) {
        return new Asymmetric(transformation, key);
    }

    /* ********************************************
     *
     * ****************************************** */

    /**
     * Generates a new symmetric secret key.
     *
     * @param transformation the name of transformation
     * @return generated secret key
     */
    public static SecretKey newSymmetricKey(final String transformation) {
        try {
            final String algorithm = getAlgorithm(transformation);
            return KeyGenerator.getInstance(algorithm).generateKey();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a symmetric key using given the encoded secret key.
     *
     * @param transformation the name of transformation
     * @param encodedKey     the encoded secret key
     * @return the secret key
     */
    public static SecretKeySpec newSymmetricKey(final String transformation, final byte[] encodedKey) {
        final String algorithm = getAlgorithm(transformation);
        return new SecretKeySpec(encodedKey, algorithm);
    }

    /**
     * Generates a new asymmetric key pair.
     *
     * @param transformation the name of transformation
     * @return generated key pair
     */
    public static KeyPair newAsymmetricKey(final String transformation) {
        try {
            final String algorithm = getAlgorithm(transformation);
            return KeyPairGenerator.getInstance(algorithm).generateKeyPair();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a asymmetric key pair using given base64 encoded public key and private key.
     *
     * @param transformation   the name of transformation
     * @param base64PublicKey  base64 encoded public key
     * @param base64PrivateKey base64 encoded private key
     * @return the asymmetric key pair
     */
    public static KeyPair newAsymmetricKey(final String transformation, final String base64PublicKey, final String base64PrivateKey) {
        final byte[] publicKey = null != base64PublicKey ? Format.BASE64.parse(base64PublicKey) : new byte[0];
        final byte[] privateKey = null != base64PrivateKey ? Format.BASE64.parse(base64PrivateKey) : new byte[0];
        return newAsymmetricKey(transformation, publicKey, privateKey);
    }

    /**
     * Creates a asymmetric key pair using given encoded public key and private key.
     *
     * @param transformation    the name of transformation
     * @param encodedPublicKey  encoded public key
     * @param encodedPrivateKey encoded private key
     * @return the asymmetric key pair
     */
    public static KeyPair newAsymmetricKey(final String transformation, final byte[] encodedPublicKey, final byte[] encodedPrivateKey) {
        PublicKey publicKey = null;
        PrivateKey privateKey = null;
        try {
            final String algorithm = getAlgorithm(transformation);
            final KeyFactory factory = KeyFactory.getInstance(algorithm);
            if (null != encodedPublicKey && 0 < encodedPublicKey.length) {
                /* new X509EncodedKeySpec(encodedPublicKey); */
                if ("RSA".equals(algorithm)) {
                    publicKey = factory.generatePublic(new X509EncodedKeySpec(encodedPublicKey));
                } else {
                    publicKey = factory.generatePublic(new PKCS8EncodedKeySpec(encodedPublicKey));
                }
            }
            if (null != encodedPrivateKey && 0 < encodedPrivateKey.length) {
                privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(encodedPrivateKey));
            }
            if (null == publicKey && null == privateKey) {
                throw new IllegalArgumentException("public key and private key must be specify at least one");
            }
            return new KeyPair(publicKey, privateKey);
        } catch (final InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a RSA key pair using given modulus, public key exponent and private key exponent.
     *
     * @param modulus         the modulus
     * @param publicExponent  the public key exponent
     * @param privateExponent the private key exponent
     * @return the RSA key pair
     */
    public static KeyPair newRSAKey(final BigInteger modulus, final BigInteger publicExponent, final BigInteger privateExponent) {
        PublicKey publicKey = null;
        PrivateKey privateKey = null;
        try {
            final KeyFactory factory = KeyFactory.getInstance("RSA");
            if (null != publicExponent) {
                final KeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
                publicKey = factory.generatePublic(keySpec);
            }
            if (null != privateExponent) {
                final KeySpec keySpec = new RSAPrivateKeySpec(modulus, privateExponent);
                privateKey = factory.generatePrivate(keySpec);
            }
            if (null == publicKey && null == privateKey) {
                throw new IllegalArgumentException("public key and private key must be specify at least one");
            }
            return new KeyPair(publicKey, privateKey);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        } catch (final InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse the base 64 encoded open ssh public key.
     *
     * @param openSshBase64PublicKey base64 encoded open ssh public key
     * @return public key
     */
    public static PublicKey parseOpenSshRsaPublicKey(final String openSshBase64PublicKey) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(parseOpenSshPublicKey(openSshBase64PublicKey));
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        } catch (final InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse the base 64 encoded open ssh public key.
     *
     * @param base64PublicKey base64 encoded open ssh public key
     * @return public key
     */
    private static KeySpec parseOpenSshPublicKey(final String base64PublicKey) {
        final int start = base64PublicKey.indexOf("ssh-rsa ");
        final int last = base64PublicKey.lastIndexOf(' ');
        final int end = -1 < start ? base64PublicKey.indexOf(' ', start + 7 + 1) : -1;

        if (0 > end || end != last) {
            throw new IllegalArgumentException("is not a OpenSSH public key");
        }
        final byte[] bytes = Base64.decode(base64PublicKey.substring(start + 7 + 1, end));

        int offset = 0;
        final int publicExponentLength = new BigInteger(Arrays.copyOfRange(bytes, offset, offset += 4)).intValue();
        final BigInteger publicExponent = new BigInteger(Arrays.copyOfRange(bytes, offset, offset += publicExponentLength));
        final int modulusLength = new BigInteger(Arrays.copyOfRange(bytes, offset, offset += 4)).intValue();
        final BigInteger modulus = new BigInteger(Arrays.copyOfRange(bytes, offset, offset + modulusLength));

        return new RSAPublicKeySpec(modulus, publicExponent);
    }

    /**
     * Gets algorithm of the transformation.
     * <p>
     * Transformation format: "algorithm/mode/padding" or "algorithm".
     * eg:
     * - AES
     * - AES/CBC/NoPadding
     * - AES/CBC/PKCS5Padding
     * - RSA/ECB/PKCS1Padding
     *
     * @param transformation the name of transformation
     * @return the algorithm of the transformation
     */
    private static String getAlgorithm(final String transformation) {
        final int i = null != transformation ? transformation.indexOf('/') : -1;
        return -1 < i ? transformation.substring(0, i) : transformation;
    }
}
