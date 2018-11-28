package freework.crypto;

import freework.crypto.cipher.Crypt;

import java.security.*;
import java.security.cert.Certificate;

/**
 * Digital signatures for authentication and integrity assurance of digital data.
 *
 * @author vacoor
 * @see Crypt#newAsymmetricKey(String)
 * @see Crypt#newSymmetricKey(String)
 * @since 1.0
 */
@SuppressWarnings("PMD.ClassNamingShouldBeCamelRule")
public class Signer {
    /**
     * MD5withRSA algorithm.
     */
    private static final String MD5_WITH_RSA = "MD5withRSA";

    /**
     * SHA1withRSA algorithm.
     */
    private static final String SHA1_WITH_RSA = "SHA1withRSA";

    /**
     * SHA256withRSA algorithm.
     */
    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    /**
     * SHA512withRSA algorithm.
     */
    private static final String SHA512_WITH_RSA = "SHA512withRSA";

    /* ****************************
     *
     * ************************** */

    /**
     * MD5withRSA algorithm signer.
     */
    public static class MD5withRSA extends Signer {

        /**
         * Creates a Signer object using the MD5withRSA signature algorithm.
         *
         * @param keyPair the key pair
         */
        public MD5withRSA(final KeyPair keyPair) {
            super(MD5_WITH_RSA, keyPair);
        }

        /**
         * Creates a Signer object using the MD5withRSA signature algorithm.
         *
         * @param cert       the certificate for verification
         * @param privateKey the private key for signing
         */
        public MD5withRSA(final Certificate cert, final PrivateKey privateKey) {
            super(MD5_WITH_RSA, cert, privateKey);
        }

        /**
         * Creates a Signer object using the MD5withRSA signature algorithm.
         *
         * @param publicKey  the public key for verification
         * @param privateKey the private key for signing
         */
        public MD5withRSA(final PublicKey publicKey, final PrivateKey privateKey) {
            super(MD5_WITH_RSA, publicKey, privateKey);
        }
    }

    /**
     * SHA1withRSA algorithm signer.
     */
    public static class SHA1withRSA extends Signer {

        /**
         * Creates a Signer object using the SHA1withRSA signature algorithm.
         *
         * @param keyPair the key pair
         */
        public SHA1withRSA(final KeyPair keyPair) {
            super(SHA1_WITH_RSA, keyPair);
        }

        /**
         * Creates a Signer object using the SHA1withRSA signature algorithm.
         *
         * @param cert       the certificate for verification
         * @param privateKey the private key for signing
         */
        public SHA1withRSA(final Certificate cert, final PrivateKey privateKey) {
            super(SHA1_WITH_RSA, cert, privateKey);
        }

        /**
         * Creates a Signer object using the SHA1withRSA signature algorithm.
         *
         * @param publicKey  the public key for verification
         * @param privateKey the private key for signing
         */
        public SHA1withRSA(final PublicKey publicKey, final PrivateKey privateKey) {
            super(SHA1_WITH_RSA, publicKey, privateKey);
        }
    }

    /**
     * SHA256withRSA algorithm signer (SHA256withRSA implementations on JDK1.7).
     */
    public static class SHA256withRSA extends Signer {

        /**
         * Creates a Signer object using the SHA256withRSA signature algorithm.
         *
         * @param keyPair the key pair
         */
        public SHA256withRSA(final KeyPair keyPair) {
            super(SHA256_WITH_RSA, keyPair);
        }

        /**
         * Creates a Signer object using the SHA256withRSA signature algorithm.
         *
         * @param cert       the certificate for verification
         * @param privateKey the private key for signing
         */
        public SHA256withRSA(final Certificate cert, final PrivateKey privateKey) {
            super(SHA256_WITH_RSA, cert, privateKey);
        }

        /**
         * Creates a Signer object using the SHA256withRSA signature algorithm.
         *
         * @param publicKey  the public key for verification
         * @param privateKey the private key for signing
         */
        public SHA256withRSA(final PublicKey publicKey, final PrivateKey privateKey) {
            super(SHA256_WITH_RSA, publicKey, privateKey);
        }
    }

    /**
     * SHA512withRSA algorithm signer (SHA512withRSA implementations on JDK1.7).
     */
    public static class SHA512withRSA extends Signer {

        /**
         * Creates a Signer object using the SHA512withRSA signature algorithm.
         *
         * @param keyPair the key pair
         */
        public SHA512withRSA(final KeyPair keyPair) {
            super(SHA512_WITH_RSA, keyPair);
        }

        /**
         * Creates a Signer object using the SHA512withRSA signature algorithm.
         *
         * @param cert       the certificate for verification
         * @param privateKey the private key for signing
         */
        public SHA512withRSA(final Certificate cert, final PrivateKey privateKey) {
            super(SHA512_WITH_RSA, cert, privateKey);
        }

        /**
         * Creates a Signer object using the SHA512withRSA signature algorithm.
         *
         * @param publicKey  the public key for verification
         * @param privateKey the private key for signing
         */
        public SHA512withRSA(final PublicKey publicKey, final PrivateKey privateKey) {
            super(SHA512_WITH_RSA, publicKey, privateKey);
        }
    }

    /**
     * Signature algorithm provider.
     */
    private Provider provider;

    /**
     * Algorithm name.
     */
    private String algorithm;

    /**
     * Private key.
     */
    private PrivateKey privateKey;

    /**
     * Public key.
     */
    private PublicKey publicKey;

    /**
     * Creates a Signer object that implements the specified signature algorithm.
     *
     * @param algorithm the name of algorithm
     * @param keyPair   the key pair
     */
    public Signer(final String algorithm, final KeyPair keyPair) {
        this(algorithm, keyPair.getPublic(), keyPair.getPrivate());
    }

    /**
     * Creates a Signer object that implements the specified signature algorithm.
     *
     * @param algorithm  the name of algorithm
     * @param cert       the verify certificate
     * @param privateKey the private key
     */
    public Signer(final String algorithm, final Certificate cert, final PrivateKey privateKey) {
        this(algorithm, cert.getPublicKey(), privateKey);
    }

    /**
     * Creates a Signer object that implements the specified signature algorithm.
     *
     * @param algorithm  the name of algorithm
     * @param publicKey  the verify public key
     * @param privateKey the private key
     */
    public Signer(final String algorithm, final PublicKey publicKey, final PrivateKey privateKey) {
        this(null, algorithm, publicKey, privateKey);
    }

    /**
     * Creates a Signer object that implements the specified signature algorithm.
     *
     * @param provider   the algorithm provider
     * @param algorithm  the name of algorithm
     * @param publicKey  the verify public key
     * @param privateKey the private key
     */
    public Signer(final Provider provider, final String algorithm, final PublicKey publicKey, final PrivateKey privateKey) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /**
     * Signing given source data.
     *
     * @param bytes the source data
     * @return the sign data
     */
    public byte[] sign(final byte[] bytes) {
        return sign(bytes, 0, bytes.length);
    }

    /**
     * Signing given source data.
     *
     * @param bytes  the source data
     * @param offset the source offset
     * @param len    the source length
     * @return the signature data
     */
    public byte[] sign(final byte[] bytes, final int offset, final int len) {
        try {
            final Signature signature = getSignature(algorithm);
            signature.initSign(privateKey);
            signature.update(bytes, offset, len);
            return signature.sign();
        } catch (final SignatureException e) {
            throw new IllegalStateException(e);
        } catch (final InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * verify signature data.
     *
     * @param rawBytes       the source data
     * @param signatureBytes the signature data
     * @return the signature data is invalid?
     */
    public boolean verify(final byte[] rawBytes, final byte[] signatureBytes) {
        return verify(rawBytes, 0, rawBytes.length, signatureBytes, 0, signatureBytes.length);
    }

    /**
     * verify signature data.
     *
     * @param rawBytes        the source data
     * @param rawOffset       the source offset
     * @param rawLength       the source length
     * @param signatureBytes  the signature data
     * @param signatureOffset the signature offset
     * @param signatureLength the signature length
     * @return the signature data is invalid?
     */
    public boolean verify(final byte[] rawBytes, final int rawOffset, final int rawLength,
                          final byte[] signatureBytes, final int signatureOffset, final int signatureLength) {
        try {
            final Signature signature = getSignature(algorithm);
            signature.initVerify(publicKey);
            signature.update(rawBytes, rawOffset, rawLength);
            return signature.verify(signatureBytes, signatureOffset, signatureLength);
        } catch (final SignatureException e) {
            throw new IllegalStateException(e);
        } catch (final InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Instantiate Signature using given algorithm.
     *
     * @param algorithm the name of algorithm
     * @return Signature object
     */
    protected Signature getSignature(final String algorithm) {
        try {
            return null != provider ? Signature.getInstance(algorithm, provider) : Signature.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the name of algorithm.
     *
     * @return the name of algorithm
     */
    public String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Gets the private key.
     *
     * @return the private key
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Gets the public key.
     *
     * @return the public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
