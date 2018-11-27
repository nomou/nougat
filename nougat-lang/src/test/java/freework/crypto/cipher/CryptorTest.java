package freework.crypto.cipher;

import freework.codec.Base64;
import freework.codec.Hex;
import freework.crypto.digest.Hash;
import freework.io.IOUtils;
import freework.util.Bytes;
import org.junit.Test;

import java.io.*;
import java.security.DigestInputStream;
import java.security.KeyPair;

import static org.junit.Assert.assertTrue;

/**
 */
public class CryptorTest {

    @Test
    public void testRsa() throws IOException {
        final KeyPair keyPair = Crypt.newAsymmetricKey("RSA");
        final Crypt asymmetry = Crypt.getAsymmetric(keyPair);
        final String plainText = "This is plain text";
        final String encrypt = asymmetry.encrypt(plainText);
        final String decrypt = asymmetry.decrypt(encrypt);

        assertTrue("cannot decrypt plain text", plainText.equals(decrypt));

        System.out.println(encrypt);
        System.out.println(decrypt);

        final byte[] ciphertext = Bytes.toBytes(Base64.decode(encrypt));

        String s = new Hash.MD5(plainText).toHex();
        System.out.println(s);

        DigestInputStream in = Hash.wrap(Hash.Algorithm.MD5, new ByteArrayInputStream(Bytes.toBytes(plainText)));
        IOUtils.flow(in, new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
            }
        }, true, true);

        System.out.println(Hex.encode(in.getMessageDigest().digest()));
    }
}