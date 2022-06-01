package freework.codec;

import freework.io.IOUtils;
import freework.net.Http;
import org.junit.Test;

import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by vacoor on 2018/11/20.
 */
public class Base64Test {
    static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String encodeIn(final byte[] bytes, final boolean rfc2045) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.flow(Base64.wrap(new ByteArrayInputStream(bytes), true, rfc2045 ? Base64.MIME : Base64.DEFAULT), out, true, true);
        return new String(out.toByteArray(), UTF_8);
    }

    public static byte[] decodeIn(final String text, final boolean rfc2045) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.flow(Base64.wrap(new ByteArrayInputStream(text.getBytes(UTF_8)), false, rfc2045 ? Base64.MIME : Base64.DEFAULT), out, true, true);
        return out.toByteArray();
    }

    public static String encodeOut(final byte[] bytes, final boolean rfc2045) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.flow(new ByteArrayInputStream(bytes), Base64.wrap(out, true, rfc2045 ? Base64.MIME : Base64.DEFAULT), true, true);
        return new String(out.toByteArray(), UTF_8);
    }

    public static byte[] decodeOut(final String text, final boolean rfc2045) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.flow(new ByteArrayInputStream(text.getBytes(UTF_8)), Base64.wrap(out, false, rfc2045 ? Base64.MIME : Base64.DEFAULT), true, true);
        return out.toByteArray();
    }

    public static byte[] generateBytes() {
        final SecureRandom random = new SecureRandom();
        return random.generateSeed(random.nextInt(100000));
    }

    /*
    public static byte[] jdkDecode(final String text) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.flow(
                java.util.Base64.getMimeDecoder().wrap(new ByteArrayInputStream(text.getBytes(UTF_8))),
                out,
                true, true
        );
        return out.toByteArray();
    }

    public static String jdkEncode(final byte[] bytes) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.flow(
                new ByteArrayInputStream(bytes),
                java.util.Base64.getMimeEncoder().wrap(out),
                true, true
        );
        return new String(out.toByteArray(), UTF_8);
    }
    */

    @Test
    public void testOut() throws IOException {
        byte[] a = {10};
        System.out.println(Arrays.toString(a.clone()));
        /*
        for (int i = 0; i < 10000; i++) {
            final byte[] bytes = generateBytes();
            final byte[] encoded = Base64.encode(bytes);
            assertArrayEquals(encoded, Base64.encode(bytes));
            assertArrayEquals(Base64.encodeToChars(bytes, false), Base64.encodeToChars(bytes, false));
            assertArrayEquals(Base64.encodeToChars(bytes, true), Base64.encodeToChars(bytes, true));
            assertEquals(Base64.encodeToString(bytes), Base64.encodeToString(bytes));

            assertArrayEquals(Base64.decode(encoded), Base64.decode(encoded));
        }
        */
        /*
        final byte[] encodedBytes = ("zFqMhDg=").getBytes();
        final byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedBytes);
        // [-52, 90, -116, -124, 56]
        System.out.println(Arrays.toString(decodedBytes));

        // final InputStream jdkDecodeIn = Base64.wrap(new ByteArrayInputStream(encodedBytes));
        final InputStream jdkDecodeIn = java.util.Base64.getDecoder().wrap(new ByteArrayInputStream(encodedBytes));
        final ByteArrayOutputStream jdkDecodeOut = new ByteArrayOutputStream();

        int len;
        byte[] buff = new byte[encodedBytes.length / 4 * 3 - 2];
        while (-1 != (len = jdkDecodeIn.read(buff, 0, buff.length))) {
            jdkDecodeOut.write(buff, 0, len);
        }
        jdkDecodeOut.flush();
        jdkDecodeIn.close();
        jdkDecodeOut.close();

        // [-52, 90, -116, -124, 56, 0, 0]
        System.out.println(Arrays.toString(jdkDecodeOut.toByteArray()));
        */
    }

    public static void main(String[] args) throws IOException {
        final int count = 384;
        HttpURLConnection httpUrlConnection = null;
        try {
            httpUrlConnection = Http.open("http://pc-v3.baozun.com/platform/seq/code/generate", "POST");
            httpUrlConnection.setRequestProperty("Cookie", "experimentation_subject_id=IjdiMTIzMmE4LTY5NWQtNDA5Mi05NzVlLWNjODBhZjY0MjgwMiI%3D--63cbf2c371605efc2e89e673857a3263274e7322; TOKEN=1374l4b; UACAPPKEY=platform-vue-prod; orgId_25120=493");
            httpUrlConnection = Http.post(httpUrlConnection, "application/json", "{\"id\":1077,\"customer\":\"om\",\"entityMark\":\"com.baozun.eca.om.model.NikeReferenceNo\",\"groupCode\":null,\"startWith\":\"A\",\"endWith\":null,\"count\":" + count + "}");
            final JsonObject responseBody = (JsonObject) Http.getResponseBodyAsJson(httpUrlConnection);
            final String all = responseBody.getString("data");
            final String[] segments = all.split("\\s*,\\s*");
            for (final String segment : segments) {
                System.out.println(segment);
            }
        } finally {
            Http.close(httpUrlConnection);
        }
    }
}