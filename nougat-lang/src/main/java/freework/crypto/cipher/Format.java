package freework.crypto.cipher;

import freework.codec.Base64;
import freework.codec.Hex;

/**
 * Format.
 *
 * @author vacoor
 * @since 1.0
 */
public interface Format {
    /**
     * Hexadecimal format.
     */
    Format HEX = new Format() {

        /**
         * {@inheritDoc}
         */
        @Override
        public String format(final byte[] bytes) {
            return null != bytes ? Hex.encode(bytes) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] parse(final String text) {
            return null != text ? Hex.decode(text) : null;
        }

    };

    /**
     * Base64 format.
     */
    Format BASE64 = new Format() {

        /**
         * {@inheritDoc}
         */
        @Override
        public String format(final byte[] bytes) {
            return null != bytes ? Base64.encodeToString(bytes) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] parse(final String text) {
            return null != text ? Base64.decode(text) : null;
        }

    };

    /**
     * Formats the given byte array.
     *
     * @param bytes the byte array to format
     * @return the formatted string
     */
    String format(final byte[] bytes);

    /**
     * Parses the formatted string.
     *
     * @param text the formatted string
     * @return the decoded byte array
     */
    byte[] parse(final String text);
}
