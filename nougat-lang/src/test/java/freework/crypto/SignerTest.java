package freework.crypto;

import freework.net.Http;
import org.junit.Test;

import java.net.HttpURLConnection;

/**
 */
public class SignerTest {

    @Test
    public void testSign() throws Exception {
        /*
        final Charset charset = Charset.forName("UTF-8");
        final KeyPair kp = Crypt.newAsymmetricKey("RSA");
        final Signer signer = new Signer.MD5withRSA(kp);
        final byte[] bytes = "输入内容".getBytes(charset);
        final byte[] sign = signer.sign(bytes);
        final boolean verify = signer.verify(bytes, sign);

        assertTrue(verify);
        */
        HttpURLConnection httpUrlConnection = null;
        try {
            httpUrlConnection = Http.open("", "");

            final Http.Multipart multipart = Http.postMultipart(httpUrlConnection, Http.UTF_8);
            multipart.addTextEntry("", "").addStreamEntry("", "", "", null, 0).complete();
        } finally {
            Http.close(httpUrlConnection);
        }
    }
}