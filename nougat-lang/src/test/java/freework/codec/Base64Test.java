package freework.codec;

import freework.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
}