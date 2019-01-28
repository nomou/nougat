package freework.codec;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Encoding and decoding for the Base64 encoding scheme.
 * NOTES: <b>The class not remove because of JDK 8 Base64 streaming api bugs.</b>
 *
 * <p>
 * The implementation of this class supports the following types of Base64  as specified in
 * <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC 4648</a> and <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>.
 *
 * <p>JDK 1.8+ (testing at JDK 8, 9, 10?, 11) <code>java.util.Base64.get(Mime)Decoder().wrap(OutputStream)</code> decoding result
 * is WRONG when encoded bytes has one padding('=') and last read buffer length = reaming available length.
 * <p><blockquote><pre>
 *    final byte[] encodedBytes = ("zFqMhDg=").getBytes();
 *    final byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedBytes);
 *    // CORRECT: [-52, 90, -116, -124, 56]
 *    System.out.println(Arrays.toString(decodedBytes));
 *
 *    final InputStream jdkDecodeIn = java.util.Base64.getDecoder().wrap(new ByteArrayInputStream(encodedBytes));
 *    final ByteArrayOutputStream jdkDecodeOut = new ByteArrayOutputStream();
 *
 *    int len;
 *    byte[] buff = new byte[encodedBytes.length / 4 * 3 - 2];
 *    while (-1 != (len = jdkDecodeIn.read(buff, 0, buff.length))) {
 *        jdkDecodeOut.write(buff, 0, len);
 *    }
 *    jdkDecodeOut.flush();
 *    jdkDecodeIn.close();
 *    jdkDecodeOut.close();
 *
 *    // WRONG: [-52, 90, -116, -124, 56, 0, 0]
 *    System.out.println(Arrays.toString(jdkDecodeOut.toByteArray()));
 * </pre></blockquote>
 * <br>
 * line 934~955 already decoding over (eof = true):
 * <blockquote><pre>
 *     if (v == '=') {                  // padding byte(s)
 *        // ...
 *        if (nextin == 0) {           // only one padding byte
 *              if (len == 0) {          // no enough output space
 *                  bits >>= 8;          // shift to lowest byte
 *                  nextout = 0;
 *              } else {
 *                  b[off++] = (byte) (bits >>  8);
 *              }
 *          }
 *          eof = true;
 *        // ...
 *     }
 * </pre></blockquote>
 * But, line 910~933 repeat decoding:
 * <blockquote><pre>
 *     int v = is.read();
 *     if (v == -1) {
 *         // WARN: should return -1 (EOF) if already eof = true and nextin = 0 and off = oldOff
 *         //              return off - oldOff if already eof = true and nextin = 0
 *         eof = true;
 *         if (nextin != 18) {
 *             // ...
 *             // WARN: repeat decoding if buffer length = encodedBytes.length / 4 * 3 - 2;
 *             if (nextin == 0) {           // only one padding byte
 *                  if (len == 0) {          // no enough output space
 *                      bits >>= 8;          // shift to lowest byte
 *                      nextout = 0;
 *                  } else {
 *                      b[off++] = (byte) (bits >>  8);
 *                  }
 *              }
 *         }
 *         // ...
 *     }
 * </pre></blockquote>
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings("PMD.UndefineMagicConstantRule")
public abstract class Base64 {
    /**
     * Defaults flag using RFC4648 encoding/decoding.
     */
    public static final int DEFAULT = 0x00;

    /**
     * RFC4648 URL SAFE.
     */
    public static final int URL_SAFE = 0x01;

    /**
     * RFC2045 MIME.
     */
    public static final int MIME = 0x02;

    /* ***************** *************** */

    private static final char PADDING = '=';
    private static final char CR = '\r';
    private static final char LF = '\n';

    /**
     * Non-Base64 encoding characters.
     */
    private static final int NON_BASE64 = -1;

    /**
     * Base64 padding character: '=' (non-base64 encoding).
     */
    private static final int NON_BASE64_PADDING = -2;

    /**
     * RFC2045 '\r'.
     */
    private static final int NON_BASE64_CR = -3;

    /**
     * RFC2045 '\n'.
     */
    private static final int NON_BASE64_LF = -4;

    /**
     * Chunk size per RFC 2045 section 6.8.
     * <p>
     * The character limit does not count the trailing CRLF, but counts all other characters, including any equal signs.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section 6.8</a>
     */
    private static final int RFC2045_CHUNK_SIZE = 76;

    private static final byte[] BASE64_CHARS = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     */
    private static final byte[] BASE64_URL_CHARS = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
    };

    private static final byte[] REVERSE_BASE64_CHARS = new byte[0x100];

    private static final byte[] REVERSE_BASE64_URL_CHARS = new byte[0x100];

    static {
        Arrays.fill(REVERSE_BASE64_CHARS, (byte) NON_BASE64);
        for (int i = 0; i < BASE64_CHARS.length; i++) {
            REVERSE_BASE64_CHARS[BASE64_CHARS[i]] = (byte) i;
        }
        REVERSE_BASE64_CHARS[PADDING] = NON_BASE64_PADDING;
        REVERSE_BASE64_CHARS[CR] = NON_BASE64_CR;
        REVERSE_BASE64_CHARS[LF] = NON_BASE64_LF;

        // url safe
        Arrays.fill(REVERSE_BASE64_URL_CHARS, (byte) NON_BASE64);
        for (int i = 0; i < BASE64_URL_CHARS.length; i++) {
            REVERSE_BASE64_URL_CHARS[BASE64_URL_CHARS[i]] = (byte) i;
        }
        REVERSE_BASE64_URL_CHARS[PADDING] = NON_BASE64_PADDING;
        REVERSE_BASE64_URL_CHARS[CR] = NON_BASE64_CR;
        REVERSE_BASE64_URL_CHARS[LF] = NON_BASE64_LF;
    }

    private static final byte[] NONE_BYTES = new byte[0];

    private static final char[] NONE_CHARS = new char[0];

    /**
     * Non-instantiate.
     */
    private Base64() {
    }

    /* ******************************************
     *                  Encoding.
     * **************************************** */

    /**
     * Encodes the specified byte array into a String using the Base64 encoding scheme.
     * <p>
     * No line breaks or other white space are inserted into the encoded data.
     *
     * @param bytes the byte array to encode
     * @return A String containing the resulting Base64 encoded characters
     */
    public static String encodeToString(final byte[] bytes) {
        return encodeToString(bytes, false);
    }

    /**
     * Encodes the specified byte array into a String using the Base64 encoding scheme.
     *
     * @param bytes   the byte array to encode
     * @param lineSep insert line breaks into encoded data for rfc2045
     * @return A String containing the resulting Base64 encoded characters
     */
    public static String encodeToString(final byte[] bytes, final boolean lineSep) {
        return new String(encodeToChars(bytes, lineSep));
    }

    /**
     * Encodes the sepcified byte array into a Url-Safe String using the Base64 encoding scheme.
     *
     * @param bytes the byte array to encode
     * @return A String containing the resulting Base64 encoded characters
     */
    public static String encodeToUrlSafeString(final byte[] bytes) {
        return new String(encodeToUrlSafeChars(bytes));
    }

    /**
     * Encodes all bytes from the specified byte array into a newly-allocated byte array using the Base64 encoding scheme.
     * <p>
     * No line breaks or other white space are inserted into the encoded data.
     *
     * @param bytes the byte array to encode
     * @return A newly-allocated byte array containing the resulting encoded bytes.
     */
    public static byte[] encode(final byte[] bytes) {
        return encode(bytes, false);
    }

    /**
     * Encodes all bytes from the specified byte array into a newly-allocated byte array using the Base64 encoding scheme.
     *
     * @param bytes   the byte array to encode
     * @param lineSep insert line breaks into encoded data for rfc2045
     * @return A newly-allocated byte array containing the resulting encoded bytes.
     */
    public static byte[] encode(final byte[] bytes, final boolean lineSep) {
        return doEncode(bytes, BASE64_CHARS, lineSep);
    }

    /**
     * Encodes all bytes from the specified byte array into a newly-allocated byte array using given Base64 encoding mapping.
     *
     * @param bytes  the byte array to encode
     * @param base64 the base64 encoding mapping
     * @param mime   insert line breaks into encoded data for rfc2045
     * @return A newly-allocated byte array containing the resulting encoded bytes.
     */
    private static byte[] doEncode(final byte[] bytes, final byte[] base64, final boolean mime) {
        final int len = null != bytes ? bytes.length : 0;
        if (0 == len) {
            return NONE_BYTES;
        }

        final int evenlen = (len / 3) * 3;
        final int cnt = ((len - 1) / 3 + 1) << 2;
        final int destlen = cnt + (mime ? (cnt - 1) / 76 << 1 : 0);
        final byte[] dest = new byte[destlen];

        for (int s = 0, d = 0, cc = 0; s < evenlen; ) {
            final int i = (bytes[s++] & 0xff) << 16 | (bytes[s++] & 0xff) << 8 | (bytes[s++] & 0xff);

            dest[d++] = base64[(i >>> 18) & 0x3f];
            dest[d++] = base64[(i >>> 12) & 0x3f];
            dest[d++] = base64[(i >>> 6) & 0x3f];
            dest[d++] = base64[i & 0x3f];

            if (mime && ++cc == 19 && d < destlen - 2) {
                dest[d++] = CR;
                dest[d++] = LF;
                cc = 0;
            }
        }

        final int left = len - evenlen;
        if (left > 0) {
            final int i = ((bytes[evenlen] & 0xff) << 10) | (left == 2 ? ((bytes[len - 1] & 0xff) << 2) : 0);
            dest[destlen - 4] = base64[i >> 12];
            dest[destlen - 3] = base64[(i >>> 6) & 0x3f];
            dest[destlen - 2] = left == 2 ? base64[i & 0x3f] : (byte) PADDING;
            dest[destlen - 1] = PADDING;
        }
        return dest;
    }

    /**
     * Encodes all bytes from the specified byte array into a character array using the Base64 encoding scheme.
     * <p>
     * No line breaks or other white space are inserted into the encoded data.
     *
     * @param bytes the byte array to encode
     * @return the Base64 encoded character array.
     */
    public static char[] encodeToUrlSafeChars(final byte[] bytes) {
        return doEncodeToChars(bytes, BASE64_URL_CHARS, false);
    }

    /**
     * Encodes all bytes from the specified byte array into a character array using the Base64 encoding scheme.
     *
     * @param lineSep insert line breaks into encoded data for rfc2045
     * @param bytes   the byte array to encode
     * @return the Base64 encoded character array.
     */
    public static char[] encodeToChars(final byte[] bytes, final boolean lineSep) {
        return doEncodeToChars(bytes, BASE64_CHARS, lineSep);
    }

    /**
     * Encodes all bytes from the specified byte array into a character array using the Base64 encoding mapping.
     *
     * @param bytes  the byte array to encode
     * @param base64 the base64 encoding mapping
     * @param mime   insert line breaks into encoded data for rfc2045
     * @return the Base64 encoded character array.
     */
    private static char[] doEncodeToChars(final byte[] bytes, final byte[] base64, final boolean mime) {
        final int len = bytes != null ? bytes.length : 0;
        if (len == 0) {
            return NONE_CHARS;
        }

        final int evenlen = (len / 3) * 3;
        final int cnt = ((len - 1) / 3 + 1) << 2;
        final int destLen = cnt + (mime ? (cnt - 1) / RFC2045_CHUNK_SIZE << 1 : 0);
        final char[] dest = new char[destLen];
        for (int s = 0, d = 0, cc = 0; s < evenlen; ) {
            final int i = (bytes[s++] & 0xff) << 16 | (bytes[s++] & 0xff) << 8 | (bytes[s++] & 0xff);

            dest[d++] = (char) (base64[(i >>> 18) & 0x3f] & 0xff);
            dest[d++] = (char) (base64[(i >>> 12) & 0x3f] & 0xff);
            dest[d++] = (char) (base64[(i >>> 6) & 0x3f] & 0xff);
            dest[d++] = (char) (base64[i & 0x3f] & 0xff);

            if (mime && (++cc == 19) && (d < (destLen - 2))) {
                dest[d++] = CR;
                dest[d++] = LF;
                cc = 0;
            }
        }

        /* 0 - 2. */
        final int left = len - evenlen;
        if (left > 0) {
            final int i = ((bytes[evenlen] & 0xff) << 10) | (left == 2 ? ((bytes[len - 1] & 0xff) << 2) : 0);

            dest[destLen - 4] = (char) (base64[i >> 12] & 0xff);
            dest[destLen - 3] = (char) (base64[(i >>> 6) & 0x3f] & 0xff);
            dest[destLen - 2] = left == 2 ? (char) (base64[i & 0x3f] & 0xff) : PADDING;
            dest[destLen - 1] = PADDING;
        }
        return dest;
    }

    /* ******************************************
     *                  Decoding.
     * **************************************** */

    /**
     * Decodes a Base64 encoded String into a newly-allocated byte array using the Base64 encoding scheme.
     *
     * @param base64 the string to decode
     * @return A newly-allocated byte array containing the decoded bytes.
     */
    public static byte[] decode(final String base64) {
        return decode(base64, false);
    }

    public static byte[] decode(final String base64, final boolean urlSafe) {
        return decode(base64.toCharArray(), urlSafe);
    }

    /**
     * Decodes a Base64 encoded characters into a newly-allocated byte array using the Base64 encoding scheme.
     *
     * @param base64 the characters to decode
     * @return A newly-allocated byte array containing the decoded bytes.
     */
    public static byte[] decode(final char[] base64) {
        return decode(base64, false);
    }

    /**
     * Decodes a Base64 encoded characters into a newly-allocated byte array using the Base64 encoding scheme.
     *
     * @param base64  the characters to decode
     * @param urlSafe the encoded bytes is url safe encoding
     * @return A newly-allocated byte array containing the decoded bytes.
     */
    public static byte[] decode(final char[] base64, final boolean urlSafe) {
        return doDecode(base64, urlSafe ? REVERSE_BASE64_URL_CHARS : REVERSE_BASE64_CHARS);
    }

    /**
     * Decodes all bytes from the input byte array using the Base64 encoding scheme, writing the results into
     * a newly-allocated output byte array.
     *
     * @param bytes the byte array to decode
     * @return A newly-allocated byte array containing the decoded bytes.
     */
    public static byte[] decode(final byte[] bytes) {
        return decode(bytes, false);
    }

    /**
     * Decodes all bytes from the input byte array using the Base64 encoding scheme, writing the results into
     * a newly-allocated output byte array.
     *
     * @param bytes   the byte array to decode
     * @param urlSafe the encoded bytes is url safe encoding
     * @return A newly-allocated byte array containing the decoded bytes.
     */
    public static byte[] decode(final byte[] bytes, final boolean urlSafe) {
        return doDecode(bytes, urlSafe ? REVERSE_BASE64_URL_CHARS : REVERSE_BASE64_CHARS);
    }

    /**
     * Decodes all bytes from the input byte array using the Base64 decoding mapping, writing the results into
     * a newly-allocated output byte array.
     *
     * @param bytes  the byte array to decode
     * @param base64 the base64 decoding mapping
     * @return A newly-allocated byte array containing the decoded bytes.
     */
    private static byte[] doDecode(final byte[] bytes, final byte[] base64) {
        final int length = bytes.length;
        if (length == 0) {
            return NONE_BYTES;
        }

        int sndx = 0;
        final int endx = length - 1;
        final int pad = bytes[endx] == PADDING ? (bytes[endx - 1] == PADDING ? 2 : 1) : 0;
        final int cnt = endx - sndx + 1;
        final int sepCnt = length > 76 ? (bytes[76] == CR ? cnt / 78 : 0) << 1 : 0;
        final int len = ((cnt - sepCnt) * 6 >> 3) - pad;
        final byte[] dest = new byte[len];

        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen; ) {
            final int i = base64[bytes[sndx++]] << 18 | base64[bytes[sndx++]] << 12
                    | base64[bytes[sndx++]] << 6 | base64[bytes[sndx++]];

            dest[d++] = (byte) (i >> 16);
            dest[d++] = (byte) (i >> 8);
            dest[d++] = (byte) i;

            if (sepCnt > 0 && ++cc == 19) {
                sndx += 2;
                cc = 0;
            }
        }
        if (d < len) {
            int i = 0;
            for (int j = 0; sndx <= endx - pad; j++) {
                i |= base64[bytes[sndx++]] << (18 - j * 6);
            }
            for (int r = 16; d < len; r -= 8) {
                dest[d++] = (byte) (i >> r);
            }
        }
        return dest;
    }

    /**
     * Decodes a Base64 encoded characters into a newly-allocated byte array using the Base64 decoding mapping.
     *
     * @param chars  the characters to decode
     * @param base64 the base64 decoding mapping
     * @return A newly-allocated byte array containing the decoded bytes.
     */
    private static byte[] doDecode(final char[] chars, final byte[] base64) {
        final int length = chars.length;
        if (length == 0) {
            return NONE_BYTES;
        }

        int sndx = 0;
        final int endx = length - 1;
        final int pad = chars[endx] == PADDING ? (chars[endx - 1] == PADDING ? 2 : 1) : 0;
        final int cnt = endx - sndx + 1;
        final int sepCnt = length > RFC2045_CHUNK_SIZE ? (chars[RFC2045_CHUNK_SIZE] == '\r' ? cnt / RFC2045_CHUNK_SIZE + 2 : 0) << 1 : 0;
        final int len = ((cnt - sepCnt) * 6 >> 3) - pad;
        final byte[] dest = new byte[len];

        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen; ) {
            final int i = base64[chars[sndx++]] << 18 | base64[chars[sndx++]] << 12
                    | base64[chars[sndx++]] << 6 | base64[chars[sndx++]];

            dest[d++] = (byte) (i >> 16);
            dest[d++] = (byte) (i >> 8);
            dest[d++] = (byte) i;

            if (sepCnt > 0 && ++cc == 19) {
                sndx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            int i = 0;
            for (int j = 0; sndx <= endx - pad; j++) {
                i |= base64[chars[sndx++]] << (18 - j * 6);
            }
            for (int r = 16; d < len; r -= 8) {
                dest[d++] = (byte) (i >> r);
            }
        }

        return dest;
    }

    /**
     * Returns an input stream for decoding Base64 encoded byte stream.
     *
     * @param in the input stream
     * @return the input stream for decoding the specified Base64 encoded byte stream
     */
    public static InputStream wrap(final InputStream in) {
        return wrap(in, false);
    }

    /**
     * Returns an input stream for encoding/decoding byte stream.
     *
     * @param in       the input stream
     * @param doEncode true if encode all data read from input stream, false if we should decode.
     * @return the input stream for encoding/decoding the specified byte stream
     */
    public static InputStream wrap(final InputStream in, final boolean doEncode) {
        return wrap(in, doEncode, doEncode ? DEFAULT : MIME);
    }

    /**
     * Returns an input stream for encoding/decoding byte stream.
     *
     * @param in       the input stream
     * @param doEncode true if encode all data read from input stream, false if we should decode.
     * @param flags    encoding/decoding flags
     *                 (this is one of the following: Base64#DEFAULT, Base64#URL_SAFE, Base64#MIME)
     * @return the input stream for encoding/decoding the specified byte stream
     */
    public static InputStream wrap(final InputStream in, final boolean doEncode, final int flags) {
        final boolean mime = 0 != (MIME & flags);
        final boolean urlSafe = 0 != (URL_SAFE & flags);
        final byte[] base64 = doEncode ? (!urlSafe ? BASE64_CHARS : BASE64_URL_CHARS)
                : (!urlSafe ? REVERSE_BASE64_CHARS : REVERSE_BASE64_URL_CHARS);
        return new Input(in, base64, doEncode, mime);
    }

    /**
     * Wraps an output stream for encoding byte data using the Base64 encoding scheme.
     *
     * @param out the input stream
     * @return the output stream for encoding the byte data into the specified Base64 encoded format
     */
    public static OutputStream wrap(final OutputStream out) {
        return wrap(out, true);
    }

    /**
     * Returns an output stream for encoding/decoding byte stream.
     *
     * @param out      the output stream
     * @param doEncode true if encode all data write to output stream, false if we should decode.
     * @return the output stream for encoding/decoding the specified byte stream
     */
    public static OutputStream wrap(final OutputStream out, final boolean doEncode) {
        return wrap(out, doEncode, doEncode ? DEFAULT : MIME);
    }

    /**
     * Returns an output stream for encoding/decoding byte stream.
     *
     * @param out      the output stream
     * @param doEncode true if encode all data write to output stream, false if we should decode.
     * @param flags    encoding/decoding flags
     *                 (this is one of the following: Base64#DEFAULT, Base64#URL_SAFE, Base64#MIME)
     * @return the output stream for encoding/decoding the specified byte stream
     */
    public static OutputStream wrap(final OutputStream out, final boolean doEncode, final int flags) {
        final boolean mime = 0 != (MIME & flags);
        final boolean urlSafe = 0 != (URL_SAFE & flags);
        final byte[] base64 = doEncode ? (!urlSafe ? BASE64_CHARS : BASE64_URL_CHARS)
                : (!urlSafe ? REVERSE_BASE64_CHARS : REVERSE_BASE64_URL_CHARS);
        return new Output(out, base64, doEncode, mime);
    }

    /* *******************************************************
     *         B A S E 6 4    I N P U T    S T R E A M
     * ***************************************************** */

    /**
     * Base64 encoding and decoding for input stream.
     */
    private static class Input extends FilterInputStream {
        /**
         * Symbol that represents the end of an input stream.
         */
        private static final int END_OF_INPUT = -1;

        /**
         * Buffers for <code>read</code> methods.
         */
        private final byte[] buf = new byte[1];

        /**
         * Encodes/Decodes base64 mapping.
         */
        private final byte[] base64;

        /**
         * Operations mode.
         */
        private final boolean doEncode;

        /**
         * Compatibles with RFC2045 MIME.
         */
        private final boolean mime;

        /**
         * 24-bit buffer for encoding/decoding.
         */
        private int bits = 0;

        /**
         * 24-bit buffers bits shift.
         */
        private int shift = -8;

        /**
         * Numbers of padding/bytes.
         */
        private int left = 0;

        /**
         * Line position of encoding/decoding base64 characters.
         */
        private int pos = 0;

        /**
         * Ends of the internal input stream?
         */
        private boolean eof = false;

        /**
         * The stream is closed?
         */
        private boolean closed = false;

        /**
         * @param in       input stream to wrap
         * @param doEncode true if encode all data read from input stream, false if we should decode.
         * @param mime     insert line breaks into encoded data for rfc2045
         */
        private Input(final InputStream in, final byte[] base64, final boolean doEncode, final boolean mime) {
            super(in);
            this.doEncode = doEncode;
            this.base64 = base64;
            this.mime = mime;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean markSupported() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mark(final int readlimit) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         *
         * @return
         * @throws IOException
         */
        @Override
        public int read() throws IOException {
            return END_OF_INPUT == this.read(buf, 0, 1) ? END_OF_INPUT : buf[0] & 0xff;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int read(final byte[] buffer, final int off, final int len) throws IOException {
            return doEncode ? doEncodeRead(buffer, off, len) : doDecodeRead(buffer, off, len);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                in.close();
            }
        }

        /* ******************************************
         *                  Encoding.
         * **************************************** */

        /**
         * Reads the next <code>len</code> (encoded) Base64 characters from the input stream into an array of bytes.
         *
         * @param buffer the buffer into which the data is read.
         * @param off    the start offset in the destination array <code>buffer</code>
         * @param len    the maximum number of bytes read.
         * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of the stream has been reached.
         * @throws IOException if an I/O error occurs.
         */
        private int doEncodeRead(final byte[] buffer, final int off, final int len) throws IOException {
            if (closed) {
                throw new IOException("Stream is closed");
            }

            if (eof && -8 >= shift) {
                return END_OF_INPUT;
            }

            int offset = off;
            do {
                // RFC
                while (mime && 0 > pos && offset - off < len) {
                    buffer[offset++] = (byte) (-1 != pos++ ? CR : LF);
                }

                final int read = this.doEncodeBits(buffer, offset, len - (offset - off));

                offset += read;
                if (mime && !eof && RFC2045_CHUNK_SIZE <= (pos += read)) {
                    pos = -2;
                }

                if (eof || -8 != shift) {
                    continue;
                }

                bits = 0;
                for (int i = 0; i < 3; i++) {
                    final int b = in.read();
                    if (END_OF_INPUT == b) {
                        /*-
                         * padding:
                         * --------
                         * left 1: 000000 000000 0000AB CDEFGH
                         *         ABCDEF GH0000 000000 000000 -- left shift 16
                         *         000000 000000 000000 ABCDEF -- right shift 18,        byte 1 = ABCDEF
                         *         000000 000000 ABCDEF GH0000 -- right shift 12 & 0x3f  byte 2 = GH0000
                         *                                                               byte 3/4 = '='
                         * ----------------------------------------------------------------------------------
                         * left 2: 000000 000ABC DEGHZY XWVUTS
                         *         ABCDEF GHZYXW VUTS00 000000 -- left shift 8
                         *         000000 000000 000000 ABCDEF -- right shift 18,        byte 1 = ABCDEF
                         *         000000 000000 ABCDEF GHZYXW -- right shift 12 & 0x3f, byte 2 = GHZYXW
                         *         000000 ABCDEF GHZYXW VUTS00 -- right shift 6 & 0x3f   byte 3 = VUTS00
                         *                                                               byte 4 = '='-
                         */
                        eof = true;
                        if (0 != i) {
                            left = i;

                            final int s = (3 - left) * 8;
                            bits <<= s;
                            shift += s;
                        }
                        break;
                    }

                    bits = bits << 8 | (b & 0xff);
                    shift += 8;
                }
            } while (-8 < shift && offset - off < len);
            return offset - off;
        }

        /**
         * Reads the next <code>len</code> (encoded) Base64 characters from the bits buffer into an array of bytes.
         *
         * @param buffer the buffer into which the data is read.
         * @param off    the start offset in the destination array <code>buffer</code>
         * @param len    the maximum number of bytes read.
         * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of the stream has been reached.
         */
        private int doEncodeBits(final byte[] buffer, final int off, final int len) {
            int offset = off;
            // shift: 16, 10, 4, -2
            while (-2 <= shift && offset < off + len) {
                // left 1: byte 3/4 is '=', left 2: byte 4 is '='
                if (1 == left && 4 >= shift) {
                    buffer[offset++] = '=';
                } else if (2 == left && -2 == shift) {
                    buffer[offset++] = '=';
                } else {
                    buffer[offset++] = base64[(bits >>> (shift + 8 - 6) & 0x3f)];
                }
                shift -= 6;
            }
            return offset - off;
        }

        /* ******************************************
         *                  Decoding.
         * **************************************** */

        /**
         * Reads the next <code>len</code> (decoded) Base64 characters from the input stream into an array of bytes.
         *
         * @param buffer the buffer into which the data is read.
         * @param off    the start offset in the destination array <code>buffer</code>
         * @param len    the maximum number of bytes read.
         * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of the stream has been reached.
         * @throws IOException if an I/O error occurs.
         */
        private int doDecodeRead(final byte[] buffer, final int off, final int len) throws IOException {
            if (eof && -8 >= shift) {
                return END_OF_INPUT;
            }

            int offset = off;
            do {
                offset += doDecodeBits(buffer, offset, len - (offset - off));
                if (-8 != shift) {
                    continue;
                }

                bits = 0;
                for (int i = 0; i < 4; i++) {
                    final int read = in.read();
                    if (read == END_OF_INPUT) {
                        if (0 != i) {
                            throw new IOException("invalid eof");
                        }
                        eof = true;
                        break;
                    }

                    final byte b = base64[read];
                    if (NON_BASE64 == b) {
                        throw new IOException("Illegal base64 ending sequence:" + read);
                    } else if (NON_BASE64_CR >= b) {
                        // RFC2045 skip CRLF, otherwise throw error.
                        if (!mime || 0 != i) {
                            throw new IOException("Illegal base64 ending sequence:" + read);
                        }
                        i--;
                    } else if (NON_BASE64_PADDING == b) {
                        if (2 <= left) {
                            throw new IOException("Illegal base64 ending sequence:" + read);
                        }
                        left++;
                        if (3 == i) {
                            if (1 == left) {
                                bits >>>= 6 - 4;
                                shift -= 6 - 4;
                            } else if (2 == left) {
                                bits >>>= 4;
                                shift -= 4;
                            } else {
                                throw new IOException("Illegal base64 ending sequence:" + read);
                            }
                        }
                    } else if (0 != left) {
                        throw new IOException("Illegal base64 ending sequence:" + read);
                    } else {
                        bits = (bits << 6) | (b & 0x3f);
                        shift += 6;
                    }
                }
            } while (!eof && -8 <= shift && offset - off < len);
            return offset - off;
        }

        /**
         * Reads the next <code>len</code> (decoded) Base64 characters from the bits buffer into an array of bytes.
         *
         * @param buffer the buffer into which the data is read.
         * @param off    the start offset in the destination array <code>buffer</code>
         * @param len    the maximum number of bytes read.
         * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of the stream has been reached.
         */
        private int doDecodeBits(final byte[] buffer, final int off, final int len) {
            int offset = off;
            while (-8 < shift && offset < off + len) {
                buffer[offset++] = (byte) (bits >> (shift) & 0xff);
                shift -= 8;
            }
            return offset - off;
        }
    }

    /* *******************************************************
     *         B A S E 6 4    O U T P U T    S T R E A M
     * ***************************************************** */

    /**
     * Base64 encoding and decoding for output stream.
     */
    private static class Output extends FilterOutputStream {
        /**
         * Encodes/Decodes base64 mapping.
         */
        private final byte[] base64;

        /**
         * Operations mode.
         */
        private final boolean doEncode;

        /**
         * Compatibles with RFC2045.
         */
        private final boolean mime;

        /**
         * 24-bit buffer for encoding/decoding.
         */
        private int bits = 0;

        /**
         * 24-bit buffers bits shift.
         */
        private int shift = -8;

        /**
         * Numbers of padding/bytes.
         */
        private int left = 0;

        /**
         * Line position of encoding/decoding base64 characters.
         */
        private int pos = 0;

        /**
         * The stream is closed?
         */
        private boolean closed = false;

        private Output(final OutputStream out, final byte[] base64, final boolean doEncode, final boolean mime) {
            super(out);
            this.doEncode = doEncode;
            this.base64 = base64;
            this.mime = mime;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final int b) throws IOException {
            this.write(new byte[]{(byte) (b & 0xff)}, 0, 1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final byte[] buf, final int off, final int len) throws IOException {
            if (closed) {
                throw new IOException("Stream is closed");
            }
            if (doEncode) {
                this.doEncodeWrite(buf, off, len);
            } else {
                this.doDecodeWrite(buf, off, len);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                if (doEncode) {
                    this.doEncodeClose();
                }
            }
        }

        /* ******************************************
         *                  Encoding.
         * **************************************** */

        /**
         * Encodes <code>len</code> bytes from the specified <code>byte</code> array starting
         * at offset <code>off</code> and write (encoded) Base64 characters to the output stream.
         *
         * @param buffer the data.
         * @param off    the start offset in the data.
         * @param len    the number of bytes to write.
         * @throws IOException if an I/O error occurs.
         */
        private void doEncodeWrite(final byte[] buffer, final int off, final int len) throws IOException {
            for (int i = 0; 16 > shift && i < len; i++) {
                if (mime && RFC2045_CHUNK_SIZE <= pos) {
                    pos = 0;
                    out.write(CR);
                    out.write(LF);
                }

                bits = (bits << 8) | (buffer[off + i] & 0xff);
                shift += 8;

                // 24-bit buffer is full
                if (16 <= shift) {
                    out.write(base64[bits >>> 18 & 0x3f]);
                    out.write(base64[bits >>> 12 & 0x3f]);
                    out.write(base64[bits >>> 6 & 0x3f]);
                    out.write(base64[bits & 0x3f]);
                    bits = 0;
                    shift -= 24;
                    pos += 4;
                }
            }
        }

        /**
         * Closes the encode output stream.
         *
         * @throws IOException if an I/O error occurs.
         */
        private void doEncodeClose() throws IOException {
            if (0 == shift || 8 == shift) {
                if (mime && RFC2045_CHUNK_SIZE <= pos) {
                    pos = 0;
                    out.write(CR);
                    out.write(LF);
                }

                final int buffer = 0 == shift ? bits << 10 : bits << 2;

                out.write(base64[buffer >>> 12]);
                out.write(base64[(buffer >>> 6) & 0x3f]);
                out.write(0 == shift ? PADDING : base64[(buffer & 0x3f)]);
                out.write(PADDING);
            }
        }

        /* ******************************************
         *                  Decoding.
         * **************************************** */

        /**
         * Decodes <code>len</code> bytes from the specified (encoded) Base64 characters starting
         * at offset <code>off</code> and write (decoded) byte array to the output stream.
         *
         * @param buffer the data.
         * @param off    the start offset in the data.
         * @param len    the number of bytes to write.
         * @throws IOException if an I/O error occurs.
         */
        private void doDecodeWrite(final byte[] buffer, final int off, final int len) throws IOException {
            // -8, -2, 4, 10, 16,
            for (int i = 0; 16 > shift && i < len; i++) {
                final byte b = base64[buffer[off + i] & 0xff];
                if (NON_BASE64 == b) {
                    throw new IOException("Illegal base64 ending sequence:" + buffer[off + i]);
                } else if (NON_BASE64_CR >= b) {
                    // RFC2045 skip CR & LF, otherwise throw error.
                    final boolean overflow = 0 < pos && RFC2045_CHUNK_SIZE > pos;
                    if (overflow && !mime) {
                        throw new IOException("Illegal base64 ending sequence:" + buffer[off + i]);
                    }
                    pos = 0;
                } else if (NON_BASE64_PADDING == b) {
                    if (2 <= left) {
                        throw new IOException("Illegal base64 ending sequence:" + buffer[off + i]);
                    }
                    left++;
                } else if (0 != left) {
                    throw new IOException("Illegal base64 ending sequence:" + buffer[off + i]);
                } else {
                    pos++;
                    shift += 6;
                    bits = (bits << 6) | (b & 0x3f);
                }

                // 24-bit buffer is full.
                if (16 == shift) {
                    out.write(bits >> 16 & 0xff);
                    out.write(bits >> 8 & 0xff);
                    out.write(bits & 0xff);

                    bits = 0;
                    shift = -8;
                }
            }
            if (1 == left && 10 == shift) {
                out.write(bits >>> 10 & 0xff);
                out.write(bits >>> 2 & 0xff);
            } else if (2 == left && 4 == shift) {
                out.write(bits >>> 4 & 0xff);
            }
        }
    }
}
